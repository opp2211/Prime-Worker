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
import ru.maltsev.primeworker.dd373.Dd373Service;
import ru.maltsev.primeworker.dd373.dto.Dd373PriceDto;
import ru.maltsev.primeworker.funpay.FunpayService;
import ru.maltsev.primeworker.funpay.dto.FunpayOfferDto;
import ru.maltsev.primeworker.g2g.G2gService;
import ru.maltsev.primeworker.g2g.dto.G2gOfferDto;
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
    private static final String FUNPAY_LEAGUE = "Runes of Aldur";
    private static final String FUNPAY_SIDE = "\u0411\u043e\u0436\u0435\u0441\u0442\u0432\u0435\u043d\u043d\u044b\u0435 \u0441\u0444\u0435\u0440\u044b";

    private final BybitP2pClient bybitClient;
    private final BinanceP2pClient binanceClient;
    private final FunpayService funpayService;
    private final Dd373Service dd373Service;
    private final G2gService g2gService;

    public RatesSnapshot calculateRates() {
        BigDecimal usdToRub = bybitPrice(
                FiatCurrency.RUB,
                List.of(BybitPaymentMethod.BANK_TRANSFER.getCode()),
                new BigDecimal("10000"),
                6
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

        BigDecimal funpayRub = funpayPrice(5);
        BigDecimal dd373Merchant = dd373MerchantPrice(1);
        BigDecimal dd373Seller = dd373SellerPrice(1);
        BigDecimal g2gUsd = getG2gUsdPrice(1)
                .setScale(4, RoundingMode.DOWN);

        List<RateValue> values = List.of(
                new RateValue(RateKind.USD_RUB, usdToRub),
                new RateValue(RateKind.RUB_USD, rubToUsd),
                new RateValue(RateKind.KZT_RUB, kztToRub),
                new RateValue(RateKind.RUB_KZT, rubToKzt),
                new RateValue(RateKind.USDT_CNY, usdtToCny),
                new RateValue(RateKind.FUNPAY_RUB, funpayRub),
                new RateValue(RateKind.DD373_MERCHANT, dd373Merchant),
                new RateValue(RateKind.DD373_SELLER, dd373Seller),
                new RateValue(RateKind.G2G_USD, g2gUsd)
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

    private BigDecimal funpayPrice(int position) {
        List<FunpayOfferDto> offers = funpayService.getOffers(FUNPAY_LEAGUE, FUNPAY_SIDE, true);
        if (offers.size() < position) {
            return BigDecimal.ZERO;
        }
        FunpayOfferDto offer = offers.get(position - 1);
        return offer.getPriceRub();
    }

    private BigDecimal dd373MerchantPrice(int position) {
        List<Dd373PriceDto> prices = dd373Service.getMerchantPrices();
        if (prices.size() < position) {
            return BigDecimal.ZERO;
        }
        return prices.get(position - 1).getPricePerStone();
    }

    private BigDecimal dd373SellerPrice(int position) {
        List<Dd373PriceDto> prices = dd373Service.getSellerPrices();
        if (prices.size() < position) {
            return BigDecimal.ZERO;
        }
        return prices.get(position - 1).getPricePerStone();
    }

    private BigDecimal getG2gUsdPrice(int position) {
        try {
            List<G2gOfferDto> offers = g2gService.getOffers();
            if (offers == null || offers.size() < position) {
                log.warn("Failed to get G2G USD price: not enough offers, using fallback 0");
                return BigDecimal.ZERO;
            }

            BigDecimal priceUsd = offers.get(position - 1).getPriceUsd();
            if (priceUsd == null) {
                log.warn("Failed to get G2G USD price: offer price is missing, using fallback 0");
                return BigDecimal.ZERO;
            }

            return priceUsd;
        } catch (Exception e) {
            log.warn("Failed to get G2G USD price, using fallback 0", e);
            return BigDecimal.ZERO;
        }
    }
}
