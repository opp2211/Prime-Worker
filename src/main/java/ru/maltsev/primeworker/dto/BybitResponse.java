package ru.maltsev.primeworker.dto;

import lombok.Data;

@Data
public class BybitResponse {
    private int retCode;
    private String retMsg;
    private BybitResult result;

}
