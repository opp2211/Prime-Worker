package ru.maltsev.primeworker.funpay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
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
}