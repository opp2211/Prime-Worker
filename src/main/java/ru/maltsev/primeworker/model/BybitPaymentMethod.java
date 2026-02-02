package ru.maltsev.primeworker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BybitPaymentMethod {
    BANK_TRANSFER("14"),
    ;

    private final String code;
}
