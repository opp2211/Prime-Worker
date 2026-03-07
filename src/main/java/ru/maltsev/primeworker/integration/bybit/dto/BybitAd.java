package ru.maltsev.primeworker.integration.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitAd {
    private String nickName;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String remark;
    private List<String> payments;
}
