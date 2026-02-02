package ru.maltsev.primeworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.dto.BybitAd;
import ru.maltsev.primeworker.model.BybitFiat;
import ru.maltsev.primeworker.model.BybitPaymentMethod;
import ru.maltsev.primeworker.model.BybitSide;
import ru.maltsev.primeworker.model.BybitToken;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrimeService {

    private final GoogleSheetsService googleSheetsService;
    private final BybitService bybitService;

    public void doBusinessLogic() {
        updateG5Rate();
        updateG6Rate();
        updateG7Rate();
        updateG8Rate();
    }

    private void updateG5Rate() {
        updateBuyUsdtCell("G5",
                BybitFiat.RUB,
                List.of(BybitPaymentMethod.BANK_TRANSFER),
                10_000,
                8);
    }

    private void updateG6Rate() {
        updateBuyUsdtCell("G6",
                BybitFiat.RUB,
                List.of(BybitPaymentMethod.BANK_TRANSFER),
                10_000,
                20);
    }

    private void updateG7Rate() {
        updateBuyUsdtCell("G7",
                BybitFiat.KZT,
                List.of(BybitPaymentMethod.KASPI_BANK),
                50_000,
                1);
    }

    private void updateG8Rate() {
        updateBuyUsdtCell("G8",
                BybitFiat.KZT,
                List.of(BybitPaymentMethod.KASPI_BANK),
                50_000,
                2);
    }

    private void updateBuyUsdtCell(String cellCode,
                                   BybitFiat bybitFiat,
                                   @Nullable List<BybitPaymentMethod> paymentMethods,
                                   @Nullable Integer amount,
                                   Integer adIndex) {

        BybitAd ad = bybitService.getSingleAd(
                BybitToken.USDT,
                bybitFiat,
                BybitSide.BUY,
                paymentMethods,
                amount,
                adIndex);

        log.debug(ad.toString());

        try {
            googleSheetsService.writeDataToCell(formatDoubleToString(ad.getPrice()), "Курс!"+ cellCode);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String formatDoubleToString(double value) {
        return String.valueOf(value).replace('.', ',');
    }

}
