package ru.maltsev.primeworker.config;

import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {

    @Test
    void httpClientHasConnectTimeout() {
        HttpClient httpClient = new AppConfig().httpClient();

        assertEquals(Duration.ofSeconds(5), httpClient.connectTimeout().orElseThrow());
    }
}
