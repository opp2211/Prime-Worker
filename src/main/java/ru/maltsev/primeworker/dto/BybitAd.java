package ru.maltsev.primeworker.dto;

import lombok.Data;

import java.util.List;

@Data
public class BybitAd {
    private String nickName;
    private Double price;
    private Double quantity;
    private Double minAmount;
    private Double maxAmount;
    private String remark;
    private List<String> payments;
}
