package ru.maltsev.primeworker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BybitFiat {
    RUB("RUB"),
    KZT("KZT"),
    UAH("UAH"),
    GEL("GEL"),
    BYN("BYN"),
    ;

    private final String code;
}
