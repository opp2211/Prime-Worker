package ru.maltsev.primeworker.config;

import com.google.api.services.sheets.v4.Sheets;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public Sheets mockedSheetsService() {
        return Mockito.mock(Sheets.class);
    }
}
