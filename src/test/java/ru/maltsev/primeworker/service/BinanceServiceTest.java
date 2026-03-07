package ru.maltsev.primeworker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.maltsev.primeworker.dto.BinanceResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BinanceServiceTest {

    @Autowired
    BinanceService binanceService;

    @Test
    void getFifthBuyUsdtToCny() {
        BinanceResponse top10BuyUsdtToCny = binanceService.getFifthBuyUsdtToCny();

        assertNotNull(top10BuyUsdtToCny);
    }
}