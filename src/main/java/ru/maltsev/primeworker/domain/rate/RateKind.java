package ru.maltsev.primeworker.domain.rate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RateKind {
    USD_RUB(4),
    RUB_USD(4),
    KZT_RUB(4),
    RUB_KZT(4),
    USDT_CNY(4),
    FUNPAY_RUB(4);

    private final int scale;
}
