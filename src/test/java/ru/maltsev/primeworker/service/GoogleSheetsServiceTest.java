package ru.maltsev.primeworker.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GoogleSheetsServiceTest {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    @Test
    @Disabled
    void writeDataToCell() throws IOException {
        googleSheetsService.writeDataToCell("132", "Лист19", "A1");
    }
}