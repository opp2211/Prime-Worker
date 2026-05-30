package ru.maltsev.primeworker.funpay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.maltsev.primeworker.funpay.dto.FunpayOfferDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FunpayServiceTest {

    @Autowired
    FunpayService funpayService;

    @Test
    void getOffers() {
        List<FunpayOfferDto> offers = funpayService.getOffers(null, true);
        assertNotNull(offers);
    }

    @Test
    void getOffersFiltersPoe2DivineOrbs() {
        List<FunpayOfferDto> offers = funpayService.getOffers(
                "Return of the Ancients",
                "Божественные сферы",
                true
        );

        assertFalse(offers.isEmpty());
        assertTrue(offers.stream().allMatch(FunpayOfferDto::isOnline));
        assertTrue(offers.stream().allMatch(offer -> "Return of the Ancients".equals(offer.getLeague())));
        assertTrue(offers.stream().allMatch(offer -> "Божественные сферы".equals(offer.getSide())));
        assertTrue(offers.stream().allMatch(offer -> offer.getPriceRub() != null));
    }
}
