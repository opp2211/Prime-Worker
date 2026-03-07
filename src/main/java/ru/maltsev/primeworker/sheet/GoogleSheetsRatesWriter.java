package ru.maltsev.primeworker.sheet;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.config.properties.GoogleProps;
import ru.maltsev.primeworker.domain.rate.RatesSnapshot;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GoogleSheetsRatesWriter implements RatesSheetWriter {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Yekaterinburg");

    private final String spreadsheetId;
    private final Sheets sheets;
    private final GoogleProps props;

    public GoogleSheetsRatesWriter(GoogleProps props, Sheets sheets) {
        this.spreadsheetId = props.getSpreadsheetId();
        this.sheets = sheets;
        this.props = props;
    }

    @Override
    public void updateRates(RatesSnapshot snapshot) {
        List<String> rates = snapshot.formattedValues();
        String sheetName = props.getSheetName();

        ValueRange updatedAtRange = new ValueRange()
                .setRange(sheetName + "!" + props.getUpdatedAtCell())
                .setValues(
                        List.of(
                                List.of(ZonedDateTime.now(ZONE_ID).format(DTF))
                        ));

        List<ValueRange> data = new ArrayList<>();
        data.add(updatedAtRange);

        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(data);

        if (!rates.isEmpty()) {
            int startRow = props.getRatesStartRow();
            int endRow = startRow + rates.size() - 1;
            String column = props.getRatesColumn();

            ValueRange ratesRange = new ValueRange()
                    .setRange(sheetName + "!" + column + startRow + ":" + column + endRow)
                    .setValues(
                            rates.stream()
                                    .map(value -> (Object) value)
                                    .map(List::of)
                                    .toList()
                    );
            data.add(ratesRange);
        } else {
            log.warn("Rates snapshot is empty, only updated timestamp");
        }

        try {
            sheets.spreadsheets().values()
                    .batchUpdate(spreadsheetId, body)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to update Google Sheet", e);
        }
    }
}
