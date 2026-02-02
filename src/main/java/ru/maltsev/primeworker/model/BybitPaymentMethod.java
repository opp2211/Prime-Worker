package ru.maltsev.primeworker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BybitPaymentMethod {
    BANK_TRANSFER("14"),
    KASPI_BANK("150"),
    ;

    private final String code;
}
