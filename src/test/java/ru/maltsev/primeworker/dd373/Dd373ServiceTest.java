package ru.maltsev.primeworker.dd373;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.maltsev.primeworker.dd373.dto.Dd373PriceDto;

import java.math.BigDecimal;
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
    void parseMerchantPricesReadsSingleprice() {
        String json = """
                {
                  "StatusCode": "0",
                  "StatusMsg": "ok",
                  "StatusData": {
                    "ResultCode": "0",
                    "ResultMsg": "ok",
                    "ResultData": [
                      {
                        "id": "0",
                        "shopno": "SH20260530143030-72010",
                        "trade": "receive",
                        "unit": "item",
                        "singleprice": "0.07728",
                        "amount": "125892.0000",
                        "maxamount": "50000.0000",
                        "minamount": "1200.0000",
                        "singlecount": "12.9400"
                      }
                    ]
                  }
                }
                """;

        List<Dd373PriceDto> prices = dd373Service.parsePrices(json);

        assertEquals(1, prices.size());
        Dd373PriceDto price = prices.getFirst();
        assertEquals("SH20260530143030-72010", price.getShopno());
        assertEquals(new BigDecimal("0.07728"), price.getSingleprice());
        assertEquals(new BigDecimal("50000.0000"), price.getMaxamount());
        assertEquals(new BigDecimal("12.9400"), price.getSinglecount());
    }

    @Test
    void parseSellerPricesReadsSinglepriceAndPrice() {
        String json = """
                {
                  "StatusCode": "0",
                  "StatusMsg": "ok",
                  "StatusData": {
                    "ResultCode": "0",
                    "ResultMsg": "ok",
                    "ResultData": [
                      {
                        "id": "0",
                        "shopno": "DB20260706191800-21149",
                        "trade": "seller",
                        "number": "1",
                        "unit": "item",
                        "amount": "520.0",
                        "singleprice": "0.078",
                        "price": "40.56"
                      }
                    ]
                  }
                }
                """;

        List<Dd373PriceDto> prices = dd373Service.parsePrices(json);

        assertEquals(1, prices.size());
        Dd373PriceDto price = prices.getFirst();
        assertEquals("DB20260706191800-21149", price.getShopno());
        assertEquals(new BigDecimal("1"), price.getNumber());
        assertEquals(new BigDecimal("0.078"), price.getSingleprice());
        assertEquals(new BigDecimal("40.56"), price.getPrice());
    }
}
