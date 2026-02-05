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
     * @param a1CellRef Адрес ячейки [Нотация Лист!A1]
     */
    public void writeDataToCell(Object data, String a1CellRef) throws IOException {

        ValueRange valueRange = prepareCellValueRange(data);

        sheets.spreadsheets().values()
                .update(spreadsheetId, a1CellRef, valueRange)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    public void writeDataToRow(List<Object> listData, String a1CellRef) throws IOException {

        ValueRange valueRange = prepareCellValueRange(listData);

        sheets.spreadsheets().values()
                .update(spreadsheetId, a1CellRef, valueRange)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    private ValueRange prepareCellValueRange(Object data) {
        List<List<Object>> values = List.of(
                List.of(data)
        );
        return new ValueRange().setValues(values);
    }

    private ValueRange prepareCellValueRange(List<Object> listData) {
        List<List<Object>> values = List.of(
                listData
        );
        return new ValueRange().setValues(values);
    }

}
