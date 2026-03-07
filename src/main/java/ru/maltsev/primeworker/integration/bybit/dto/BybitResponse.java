package ru.maltsev.primeworker.integration.bybit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitResponse {
    @JsonAlias({"ret_code", "retCode"})
    private int retCode;
    @JsonAlias({"ret_msg", "retMsg"})
    private String retMsg;
    private BybitResult result;
}
