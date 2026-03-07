package ru.maltsev.primeworker.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.maltsev.primeworker.domain.p2p.CryptoAsset;
import ru.maltsev.primeworker.domain.p2p.FiatCurrency;
import ru.maltsev.primeworker.domain.p2p.P2pAd;
import ru.maltsev.primeworker.domain.p2p.P2pQuery;
import ru.maltsev.primeworker.domain.p2p.TradeSide;
import ru.maltsev.primeworker.integration.bybit.BybitP2pClient;

import java.util.List;

@SpringBootTest
class BybitP2pClientTest {

    @Autowired
    private BybitP2pClient client;

    @Disabled
    @Test
    void getAds() {
        P2pQuery query = new P2pQuery(
                CryptoAsset.USDT,
                FiatCurrency.KZT,
                TradeSide.BUY,
                List.of(),
                new java.math.BigDecimal("50000"),
                null,
                null
        );
        List<P2pAd> ads = client.findAds(query);

        Assertions.assertNotNull(ads);

        System.out.println("\n========= BYBIT P2P ADS =========");
        System.out.println("Total ads: " + ads.size());
        System.out.println();

        System.out.printf(
                "%-3s %-15s %-10s %-10s %-12s %-12s %-20s %-30s%n",
                "№", "SELLER", "PRICE", "QTY", "MIN", "MAX", "PAYMENTS", "REMARK"
        );

        System.out.println("-----------------------------------------------------------------------------------------------");

        for (int i = 0; i < ads.size(); i++) {

            P2pAd ad = ads.get(i);

            String payments = (ad.payments() == null)
                    ? "-"
                    : String.join(", ", ad.payments());

            String remark = (ad.remark() == null || ad.remark().isBlank())
                    ? "-"
                    : ad.remark();

            System.out.printf(
                    "%-3d %-15s %-10.2f %-10.2f %-12.2f %-12.2f %-20s %-30s%n",
                    i + 1,
                    ad.seller(),
                    ad.price(),
                    ad.quantity(),
                    ad.minAmount(),
                    ad.maxAmount(),
                    payments,
                    remark.length() > 28 ? remark.substring(0, 28) + "..." : remark
            );
        }
    }
}
