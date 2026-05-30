package ru.maltsev.primeworker.funpay;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import ru.maltsev.primeworker.funpay.dto.FunpayOfferDto;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunpayServiceTest {

    private static final String LEAGUE = "Return of the Ancients";
    private static final String DIVINE_ORBS = "\u0411\u043e\u0436\u0435\u0441\u0442\u0432\u0435\u043d\u043d\u044b\u0435 \u0441\u0444\u0435\u0440\u044b";
    private static final String RUBLE_SIGN = "\u20bd";

    private final FunpayService funpayService = new FunpayService();

    @Test
    void parseOffersReadsLeagueSideOnlineSellerStockAndRubPrice() {
        String html = """
                <div class="tc-item">
                    <div class="tc-server">%s</div>
                    <div class="tc-side">%s</div>
                    <div class="media-user online">
                        <span class="media-user-name">seller</span>
                    </div>
                    <div class="tc-amount">1 000</div>
                    <div class="tc-price">
                        <div>10,50 %s</div>
                        <div>$0.11</div>
                    </div>
                </div>
                """.formatted(LEAGUE, DIVINE_ORBS, RUBLE_SIGN);

        List<FunpayOfferDto> offers = funpayService.parseOffers(Jsoup.parse(html));

        assertEquals(1, offers.size());
        FunpayOfferDto offer = offers.getFirst();
        assertEquals(LEAGUE, offer.getLeague());
        assertEquals(DIVINE_ORBS, offer.getSide());
        assertEquals("seller", offer.getSeller());
        assertTrue(offer.isOnline());
        assertEquals("1 000", offer.getStock());
        assertEquals("10,50 " + RUBLE_SIGN, offer.getPriceText());
        assertEquals(new BigDecimal("10.50"), offer.getPriceRub());
    }

    @Test
    void filterOffersKeepsOnlinePoe2DivineOrbsSortedByRubPrice() {
        List<FunpayOfferDto> offers = List.of(
                offer("Standard", DIVINE_ORBS, true, "1.00"),
                offer(LEAGUE, "Chaos Orb", true, "2.00"),
                offer(LEAGUE, DIVINE_ORBS, false, "3.00"),
                offer(LEAGUE, DIVINE_ORBS, true, "5.00"),
                offer(LEAGUE, DIVINE_ORBS, true, "4.00")
        );

        List<FunpayOfferDto> filteredOffers = funpayService.filterOffers(offers, LEAGUE, DIVINE_ORBS, true);

        assertEquals(2, filteredOffers.size());
        assertEquals(new BigDecimal("4.00"), filteredOffers.get(0).getPriceRub());
        assertEquals(new BigDecimal("5.00"), filteredOffers.get(1).getPriceRub());
        assertTrue(filteredOffers.stream().allMatch(FunpayOfferDto::isOnline));
        assertTrue(filteredOffers.stream().allMatch(offer -> LEAGUE.equals(offer.getLeague())));
        assertTrue(filteredOffers.stream().allMatch(offer -> DIVINE_ORBS.equals(offer.getSide())));
    }

    private FunpayOfferDto offer(String league, String side, boolean online, String priceRub) {
        return new FunpayOfferDto(
                league,
                side,
                "seller",
                online,
                "stock",
                priceRub + " " + RUBLE_SIGN,
                new BigDecimal(priceRub)
        );
    }
}
