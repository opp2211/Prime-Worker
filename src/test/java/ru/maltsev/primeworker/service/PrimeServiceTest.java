package ru.maltsev.primeworker.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PrimeServiceTest {

    @Autowired
    private PrimeService primeService;

    @Disabled
    @Test
    void updateSpreadsheetData() {
        primeService.updateSpreadsheetData();
    }
}