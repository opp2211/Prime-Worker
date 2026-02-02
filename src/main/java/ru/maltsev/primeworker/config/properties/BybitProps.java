package ru.maltsev.primeworker.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.bybit")
@Getter
@Setter
public class BybitProps {
    private String apiKey;
    private String apiSecret;
    private String recvWindow;
}
