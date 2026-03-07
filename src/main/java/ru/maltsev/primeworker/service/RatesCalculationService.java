package ru.maltsev.primeworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.domain.p2p.CryptoAsset;
import ru.maltsev.primeworker.domain.p2p.FiatCurrency;
import ru.maltsev.primeworker.domain.p2p.P2pAd;
import ru.maltsev.primeworker.domain.p2p.P2pQuery;
import ru.maltsev.primeworker.domain.p2p.TradeSide;
import ru.maltsev.primeworker.domain.rate.RateKind;
import ru.maltsev.primeworker.domain.rate.RateValue;
import ru.maltsev.primeworker.domain.rate.RatesSnapshot;
import ru.maltsev.primeworker.integration.binance.BinanceP2pClient;
import ru.maltsev.primeworker.integration.bybit.BybitP2pClient;
import ru.maltsev.primeworker.integration.bybit.BybitPaymentMethod;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatesCalculationService {

    private static final BigDecimal BYBIT_FEE_FACTOR = new BigDecimal("1.00275");
    private static final MathContext MC = new MathContext(18, RoundingMode.DOWN);

    private final BybitP2pClient bybitClient;
    private final BinanceP2pClient binanceClient;

    public RatesSnapshot calculateRates() {
        BigDecimal usdToRub = bybitPrice(
                FiatCurrency.RUB,
                List.of(BybitPaymentMethod.BANK_TRANSFER.getCode()),
                new BigDecimal("10000"),
                8
        ).divide(BYBIT_FEE_FACTOR, MC);

        BigDecimal rubToUsd = BigDecimal.ONE.divide(
                bybitPrice(
                        FiatCurrency.RUB,
                        List.of(BybitPaymentMethod.BANK_TRANSFER.getCode()),
                        new BigDecimal("10000"),
                        20
                ),
                MC
        );

        BigDecimal usdToKzt = bybitPrice(
                FiatCurrency.KZT,
                List.of(BybitPaymentMethod.KASPI_BANK.getCode()),
                new BigDecimal("50000"),
                2
        );

        BigDecimal kztToUsd = BigDecimal.ONE.divide(
                bybitPrice(
                        FiatCurrency.KZT,
                        List.of(BybitPaymentMethod.KASPI_BANK.getCode()),
                        new BigDecimal("50000"),
                        4
                ),
                MC
        );

        BigDecimal kztToRub = kztToUsd.multiply(usdToRub, MC);
        BigDecimal rubToKzt = rubToUsd.multiply(usdToKzt, MC);

        BigDecimal usdtToCny = binancePrice(
                FiatCurrency.CNY,
                5
        );

        List<RateValue> values = List.of(
                new RateValue(RateKind.USD_RUB, usdToRub),
                new RateValue(RateKind.RUB_USD, rubToUsd),
                new RateValue(RateKind.KZT_RUB, kztToRub),
                new RateValue(RateKind.RUB_KZT, rubToKzt),
                new RateValue(RateKind.USDT_CNY, usdtToCny)
        );

        return new RatesSnapshot(values);
    }

    private BigDecimal bybitPrice(FiatCurrency fiat,
                                  List<String> paymentMethods,
                                  BigDecimal amount,
                                  int position) {
        P2pQuery query = new P2pQuery(
                CryptoAsset.USDT,
                fiat,
                TradeSide.BUY,
                paymentMethods,
                amount,
                null,
                null
        );
        P2pAd ad = bybitClient.findAd(query, position);
        if (ad.price() == null) {
            throw new IllegalStateException("Bybit returned ad without price");
        }
        return ad.price();
    }

    private BigDecimal binancePrice(FiatCurrency fiat, int position) {
        P2pQuery query = new P2pQuery(
                CryptoAsset.USDT,
                fiat,
                TradeSide.BUY,
                List.of(),
                null,
                null,
                null
        );
        P2pAd ad = binanceClient.findAd(query, position);
        if (ad.price() == null) {
            throw new IllegalStateException("Binance returned ad without price");
        }
        return ad.price();
    }
}
