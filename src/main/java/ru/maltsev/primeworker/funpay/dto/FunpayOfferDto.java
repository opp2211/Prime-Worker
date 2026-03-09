package ru.maltsev.primeworker.funpay.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class FunpayOfferDto {
    private String league;
    private String seller;
    private boolean online;
    private String stock;
    private String priceText;
    private BigDecimal priceRub;

    public FunpayOfferDto() {
    }

    public FunpayOfferDto(String league, String seller, boolean online, String stock, String priceText, BigDecimal priceRub) {
        this.league = league;
        this.seller = seller;
        this.online = online;
        this.stock = stock;
        this.priceText = priceText;
        this.priceRub = priceRub;
    }

}
