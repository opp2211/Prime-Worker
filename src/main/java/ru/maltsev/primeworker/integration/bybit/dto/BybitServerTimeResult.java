package ru.maltsev.primeworker.integration.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitServerTimeResult {
    private String timeSecond;
    private String timeNano;
}
