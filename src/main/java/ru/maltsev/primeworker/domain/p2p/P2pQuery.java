package ru.maltsev.primeworker.domain.p2p;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record P2pQuery(
        CryptoAsset asset,
        FiatCurrency fiat,
        TradeSide side,
        List<String> paymentMethods,
        BigDecimal amount,
        Integer page,
        Integer pageSize
) {
    public P2pQuery {
        Objects.requireNonNull(asset, "asset");
        Objects.requireNonNull(fiat, "fiat");
        Objects.requireNonNull(side, "side");
        paymentMethods = paymentMethods == null ? List.of() : List.copyOf(paymentMethods);
    }

    public int resolvePage(int defaultPage) {
        return page == null ? defaultPage : page;
    }

    public int resolvePageSize(int defaultPageSize) {
        return pageSize == null ? defaultPageSize : pageSize;
    }
}
