package ru.maltsev.primeworker.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.config.properties.GoogleProps;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RatesGoogleSheetService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Yekaterinburg");

    private final String spreadsheetId;
    private final Sheets sheets;

    public RatesGoogleSheetService(GoogleProps props, Sheets sheets) {
        this.spreadsheetId = props.getSpreadsheetId();
        this.sheets = sheets;
    }

    public void updateRates(List<String> rates) {

        int startIndex = 3;
        int endIndex = startIndex + rates.size();


        ValueRange updatedAtRange = new ValueRange()
                .setRange("Валюты!M1")
                .setValues(
                        List.of(
                                List.of(ZonedDateTime.now(ZONE_ID).format(DTF))
                        ));
        ValueRange ratesRange = new ValueRange()
                .setRange("Валюты!J" + startIndex + ":J" + endIndex)
                .setValues(
                        rates.stream()
                                .map(value -> (Object) value)
                                .map(List::of)
                                .toList()
                );


        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(List.of(
                        updatedAtRange,
                        ratesRange
                ));

        try {
            sheets.spreadsheets().values()
                    .batchUpdate(spreadsheetId, body)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
