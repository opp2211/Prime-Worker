package ru.maltsev.primeworker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BybitSide {
    BUY("1"),
    SELL("0"),
    ;

    private final String code;
}
