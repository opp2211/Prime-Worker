package ru.maltsev.primeworker.domain.rate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RateKind {
    USD_RUB(4),
    RUB_USD(6),
    KZT_RUB(4),
    RUB_KZT(4),
    USDT_CNY(4),
    FUNPAY_RUB(2),
    DD373_MERCHANT(4),
    DD373_SELLER(4),
    G2G_USD(4);

    private final int scale;
}
