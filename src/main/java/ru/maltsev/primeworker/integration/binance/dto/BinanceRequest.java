package ru.maltsev.primeworker.integration.binance.dto;

import lombok.Data;

import java.util.List;

@Data
public class BinanceRequest {

    private String asset;        // "USDT"
    private String fiat;         // "CNY"
    private String tradeType;    // "BUY"
    private int page;            // 1
    private int rows;            // 10
    private List<String> payTypes;     // optional
    private String publisherType;// null
}
