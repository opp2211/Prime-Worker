package ru.maltsev.primeworker.g2g;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.maltsev.primeworker.g2g.dto.G2gOfferDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class G2gServiceTest {

    @Autowired
    private G2gService g2gService;

    @Test
    void getOffers() {
        List<G2gOfferDto> offers = g2gService.getOffers();
        assertNotNull(offers);
    }
}