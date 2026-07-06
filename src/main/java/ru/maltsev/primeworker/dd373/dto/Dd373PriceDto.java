package ru.maltsev.primeworker.dd373.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class Dd373PriceDto {

    private String id;
    private String shopno;
    private String trade;
    private BigDecimal number;
    private String unit;
    private BigDecimal amount;
    private BigDecimal singleprice;
    private BigDecimal maxamount;
    private BigDecimal minamount;
    private BigDecimal singlecount;
    private BigDecimal price;

    public Dd373PriceDto() {}

    public Dd373PriceDto(BigDecimal singleprice, String shopno) {
        this.singleprice = singleprice;
        this.shopno = shopno;
    }

}
