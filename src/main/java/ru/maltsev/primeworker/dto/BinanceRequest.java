package ru.maltsev.primeworker.dto;

import lombok.Data;

@Data
public class BinanceRequest {

    private String asset;        // "USDT"
    private String fiat;         // "CNY"
    private String tradeType;    // "BUY"
    private int page;            // 1
    private int rows;            // 10
    private String payTypes;     // null
    private String publisherType;// null
}