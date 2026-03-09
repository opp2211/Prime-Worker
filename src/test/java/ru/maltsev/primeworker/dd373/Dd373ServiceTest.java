package ru.maltsev.primeworker.dd373;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.maltsev.primeworker.dd373.dto.Dd373PriceDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Dd373ServiceTest {

    @Autowired
    private Dd373Service dd373Service;

    @Test
    void getMerchantPrices() {
        List<Dd373PriceDto> merchantPrices = dd373Service.getMerchantPrices();
        assertNotNull(merchantPrices);
    }

    @Test
    void getSellerPrices() {
        List<Dd373PriceDto> sellerPrices = dd373Service.getSellerPrices();
        assertNotNull(sellerPrices);
    }
}