package ru.maltsev.primeworker.dd373.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class Dd373PriceDto {

    private BigDecimal pricePerStone;
    private String priceText;

    public Dd373PriceDto() {}

    public Dd373PriceDto(BigDecimal pricePerStone, String priceText) {
        this.pricePerStone = pricePerStone;
        this.priceText = priceText;
    }

}
