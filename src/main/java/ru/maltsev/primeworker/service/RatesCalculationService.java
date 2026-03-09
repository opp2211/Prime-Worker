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
    private final FunpayService funpayService;
    private final Dd373Service dd373Service;

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

        BigDecimal funpayRub = funpayPrice(5);
        BigDecimal dd373Merchant = dd373MerchantPrice(2);
        BigDecimal dd373Seller = dd373SellerPrice(2);

        List<RateValue> values = List.of(
                new RateValue(RateKind.USD_RUB, usdToRub),
                new RateValue(RateKind.RUB_USD, rubToUsd),
                new RateValue(RateKind.KZT_RUB, kztToRub),
                new RateValue(RateKind.RUB_KZT, rubToKzt),
                new RateValue(RateKind.USDT_CNY, usdtToCny),
                new RateValue(RateKind.FUNPAY_RUB, funpayRub),
                new RateValue(RateKind.DD373_MERCHANT, dd373Merchant),
                new RateValue(RateKind.DD373_SELLER, dd373Seller)
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
        List<FunpayOfferDto> offers = funpayService.getOffers("(PC) Mirage", false);
        List<FunpayOfferDto> pricedOffers = offers.stream()
                .filter(offer -> offer.getPriceRub() != null)
                .toList();
        if (pricedOffers.size() < position) {
            throw new IllegalStateException("Funpay returned " + pricedOffers.size()
                    + " offers with price, need " + position);
        }
        FunpayOfferDto offer = pricedOffers.get(position - 1);
        return offer.getPriceRub();
    }

    private BigDecimal dd373MerchantPrice(int position) {
        List<Dd373PriceDto> prices = dd373Service.getMerchantPrices();
        List<Dd373PriceDto> priced = prices.stream()
                .filter(price -> price.getPricePerStone() != null)
                .toList();
        if (priced.size() < position) {
            throw new IllegalStateException("Dd373 merchant returned " + priced.size()
                    + " prices, need " + position);
        }
        return priced.get(position - 1).getPricePerStone();
    }

    private BigDecimal dd373SellerPrice(int position) {
        List<Dd373PriceDto> prices = dd373Service.getSellerPrices();
        List<Dd373PriceDto> priced = prices.stream()
                .filter(price -> price.getPricePerStone() != null)
                .toList();
        if (priced.size() < position) {
            throw new IllegalStateException("Dd373 seller returned " + priced.size()
                    + " prices, need " + position);
        }
        return priced.get(position - 1).getPricePerStone();
    }
}
