package ru.maltsev.primeworker.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.maltsev.primeworker.dto.BybitAd;
import ru.maltsev.primeworker.model.BybitFiat;
import ru.maltsev.primeworker.model.BybitSide;
import ru.maltsev.primeworker.model.BybitToken;

import java.util.List;

@SpringBootTest
class BybitServiceTest {

    @Autowired
    private BybitService service;

    @Test
    void getAds() {
        List<BybitAd> ads = service.getAds(BybitToken.USDT, BybitFiat.KZT, BybitSide.BUY, null, 50_000);

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

            BybitAd ad = ads.get(i);

            String payments = (ad.getPayments() == null)
                    ? "-"
                    : String.join(", ", ad.getPayments());

            String remark = (ad.getRemark() == null || ad.getRemark().isBlank())
                    ? "-"
                    : ad.getRemark();

            System.out.printf(
                    "%-3d %-15s %-10.2f %-10.2f %-12.2f %-12.2f %-20s %-30s%n",
                    i + 1,
                    ad.getNickName(),
                    ad.getPrice(),
                    ad.getQuantity(),
                    ad.getMinAmount(),
                    ad.getMaxAmount(),
                    payments,
                    remark.length() > 28 ? remark.substring(0, 28) + "..." : remark
            );
        }
    }
}