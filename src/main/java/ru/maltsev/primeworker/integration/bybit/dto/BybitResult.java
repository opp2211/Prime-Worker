package ru.maltsev.primeworker.integration.bybit.dto;

import lombok.Data;

import java.util.List;

@Data
public class BybitResult {
    private List<BybitAd> items;
}
