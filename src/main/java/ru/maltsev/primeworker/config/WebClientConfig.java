package ru.maltsev.primeworker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient bybitWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.bybit.com")
                .build();
    }
}
