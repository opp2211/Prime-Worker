package ru.maltsev.primeworker.domain.p2p;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CryptoAsset {
    USDT("USDT");

    private final String code;
}
