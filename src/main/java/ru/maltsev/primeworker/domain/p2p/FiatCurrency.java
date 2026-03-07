package ru.maltsev.primeworker.domain.p2p;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FiatCurrency {
    RUB("RUB"),
    KZT("KZT"),
    CNY("CNY"),
    UAH("UAH"),
    GEL("GEL"),
    BYN("BYN");

    private final String code;
}
