package ru.maltsev.primeworker.domain.p2p;

import java.math.BigDecimal;
import java.util.List;

public record P2pAd(
        String seller,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        List<String> payments,
        String remark
) {
}
