package ru.maltsev.primeworker.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.google")
@Getter
@Setter
public class GoogleProps {

    private String spreadsheetId;
    private String sheetName = "Валюты";
    private String ratesColumn = "J";
    private int ratesStartRow = 3;
    private String updatedAtCell = "M1";
}
