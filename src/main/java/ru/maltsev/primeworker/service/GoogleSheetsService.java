package ru.maltsev.primeworker.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.config.properties.GoogleProps;

import java.io.IOException;
import java.util.List;

@Service
public class GoogleSheetsService {

    private final String spreadsheetId;
    private final Sheets sheets;

    public GoogleSheetsService(GoogleProps props, Sheets sheets) {
        this.spreadsheetId = props.getSpreadsheetId();
        this.sheets = sheets;
    }

    /**
     * Перезаписывает содержимое ячейки
     *
     * @param data      Данные, которые необходимо записать в ячейку.
     * @param sheetName Название листа в таблицах.
     * @param cellIndex Индекс ячейки [Нотация A1]
     */
    public void writeDataToCell(String data, String sheetName, String cellIndex) throws IOException {

        String range = prepareRange(sheetName, cellIndex);
        ValueRange valueRange = prepareCellValueRange(data);

        sheets.spreadsheets().values()
                .update(spreadsheetId, range, valueRange)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    private ValueRange prepareCellValueRange(String data) {
        List<List<Object>> values = List.of(
                List.of(data)
        );
        return new ValueRange().setValues(values);
    }

    private String prepareRange(String sheetName, String cellIndex) {
        return sheetName + "!" +  cellIndex;
    }

}
