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
        assertFalse(merchantPrices.isEmpty());
    }

    @Test
    void getSellerPrices() {
        List<Dd373PriceDto> sellerPrices = dd373Service.getSellerPrices();
        assertNotNull(sellerPrices);
        assertFalse(sellerPrices.isEmpty());
    }

    @Test
    void buildAcwScV2() {
        String arg1 = "DF881C9AF3964E5A1C4C740B0F9BE6CE149AED84";

        String cookie = Dd373Service.buildAcwScV2(arg1);

        assertEquals("69eb0dbd3fc9294602dc8d99d7ec9f98360b4d1f", cookie);
    }
}
