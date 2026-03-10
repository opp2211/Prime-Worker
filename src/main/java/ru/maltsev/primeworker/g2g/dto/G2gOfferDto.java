package ru.maltsev.primeworker.g2g.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class G2gOfferDto {

    private String username;
    private BigDecimal priceUsd;

    public G2gOfferDto(String username, BigDecimal priceUsd) {
        this.username = username;
        this.priceUsd = priceUsd;
    }

}
