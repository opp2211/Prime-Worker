package ru.maltsev.primeworker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BybitToken {
    USDT("USDT"),
    ;

    private final String code;
}
