package ru.maltsev.primeworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.maltsev.primeworker.dd373.Dd373Service;
import ru.maltsev.primeworker.dd373.dto.Dd373PriceDto;
import ru.maltsev.primeworker.domain.p2p.P2pAd;
import ru.maltsev.primeworker.domain.p2p.P2pQuery;
import ru.maltsev.primeworker.domain.rate.RateKind;
import ru.maltsev.primeworker.domain.rate.RateValue;
import ru.maltsev.primeworker.domain.rate.RatesSnapshot;
import ru.maltsev.primeworker.funpay.FunpayService;
import ru.maltsev.primeworker.funpay.dto.FunpayOfferDto;
import ru.maltsev.primeworker.g2g.G2gService;
import ru.maltsev.primeworker.integration.binance.BinanceP2pClient;
import ru.maltsev.primeworker.integration.bybit.BybitP2pClient;

import java.math.BigDecimal;
import java.net.http.HttpTimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatesCalculationServiceTest {

    @Mock
    private BybitP2pClient bybitClient;

    @Mock
    private BinanceP2pClient binanceClient;

    @Mock
    private FunpayService funpayService;

    @Mock
    private Dd373Service dd373Service;

    @Mock
    private G2gService g2gService;

    @InjectMocks
    private RatesCalculationService service;

    @Test
    void calculateRatesUsesZeroWhenG2gFailsAndKeepsOtherValues() {
        stubSuccessfulSources();
        when(g2gService.getOffers()).thenThrow(new RuntimeException(new HttpTimeoutException("timeout")));

        RatesSnapshot snapshot = service.calculateRates();

        assertEquals(9, snapshot.values().size());
        assertEquals(0, value(snapshot, RateKind.G2G_USD).compareTo(BigDecimal.ZERO));
        assertEquals("0,0000", snapshot.formattedValues().get(8));
        assertEquals(new BigDecimal("12.34"), value(snapshot, RateKind.FUNPAY_RUB));
        assertEquals(new BigDecimal("0.1234"), value(snapshot, RateKind.DD373_MERCHANT));
        assertEquals(new BigDecimal("0.2345"), value(snapshot, RateKind.DD373_SELLER));
        verify(g2gService).getOffers();
    }

    @Test
    void calculateRatesUsesZeroWhenG2gReturnsEmptyOffers() {
        stubSuccessfulSources();
        when(g2gService.getOffers()).thenReturn(List.of());

        RatesSnapshot snapshot = service.calculateRates();

        assertEquals(0, value(snapshot, RateKind.G2G_USD).compareTo(BigDecimal.ZERO));
        assertEquals(new BigDecimal("7.00"), value(snapshot, RateKind.USDT_CNY));
    }

    private void stubSuccessfulSources() {
        doAnswer(invocation -> bybitAdForPosition(invocation.getArgument(1)))
                .when(bybitClient)
                .findAd(any(P2pQuery.class), anyInt());
        when(binanceClient.findAd(any(P2pQuery.class), eq(5)))
                .thenReturn(new P2pAd("binance", new BigDecimal("7.00"), null, null, null, List.of(), null));
        when(funpayService.getOffers("(PC) Mirage", true))
                .thenReturn(List.of(
                        funpayOffer("8.00"),
                        funpayOffer("9.00"),
                        funpayOffer("10.00"),
                        funpayOffer("11.00"),
                        funpayOffer("12.34")
                ));
        when(dd373Service.getMerchantPrices())
                .thenReturn(List.of(new Dd373PriceDto(new BigDecimal("0.1234"), "merchant")));
        when(dd373Service.getSellerPrices())
                .thenReturn(List.of(new Dd373PriceDto(new BigDecimal("0.2345"), "seller")));
    }

    private P2pAd bybitAdForPosition(int position) {
        BigDecimal price = switch (position) {
            case 2, 4 -> new BigDecimal("500.00");
            case 6, 20 -> new BigDecimal("100.00");
            default -> throw new IllegalArgumentException("Unexpected position: " + position);
        };
        return new P2pAd("bybit", price, null, null, null, List.of(), null);
    }

    private FunpayOfferDto funpayOffer(String price) {
        return new FunpayOfferDto(
                "(PC) Mirage",
                "funpay",
                true,
                "100",
                price,
                new BigDecimal(price)
        );
    }

    private BigDecimal value(RatesSnapshot snapshot, RateKind kind) {
        return snapshot.values().stream()
                .filter(value -> value.kind() == kind)
                .map(RateValue::value)
                .findFirst()
                .orElseThrow();
    }
}
