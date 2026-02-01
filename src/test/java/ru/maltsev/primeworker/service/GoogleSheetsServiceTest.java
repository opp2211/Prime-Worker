package ru.maltsev.primeworker.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.maltsev.primeworker.config.TestConfig;

import java.io.IOException;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class GoogleSheetsServiceTest {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    @Test
    @Disabled
    void writeDataToCell() throws IOException {
        googleSheetsService.writeDataToCell("132", "Лист19", "A1");
    }
}