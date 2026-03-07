package ru.maltsev.primeworker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.maltsev.primeworker.domain.p2p.CryptoAsset;
import ru.maltsev.primeworker.domain.p2p.FiatCurrency;
import ru.maltsev.primeworker.domain.p2p.P2pAd;
import ru.maltsev.primeworker.domain.p2p.P2pQuery;
import ru.maltsev.primeworker.domain.p2p.TradeSide;
import ru.maltsev.primeworker.integration.binance.BinanceP2pClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BinanceP2pClientTest {

    @Autowired
    BinanceP2pClient binanceClient;

    @Test
    void getFifthBuyUsdtToCny() {
        P2pQuery query = new P2pQuery(
                CryptoAsset.USDT,
                FiatCurrency.CNY,
                TradeSide.BUY,
                List.of(),
                null,
                null,
                null
        );
        P2pAd ad = binanceClient.findAd(query, 5);

        assertNotNull(ad);
    }
}
