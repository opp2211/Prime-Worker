package ru.maltsev.primeworker.dto;

import lombok.Data;

import java.util.List;

@Data
public class BybitResult {
    private List<BybitAd> items;
}
