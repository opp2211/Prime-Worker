package ru.maltsev.primeworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.dto.BybitAd;
import ru.maltsev.primeworker.model.BybitFiat;
import ru.maltsev.primeworker.model.BybitPaymentMethod;
import ru.maltsev.primeworker.model.BybitSide;
import ru.maltsev.primeworker.model.BybitToken;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrimeService {

    private final RatesGoogleSheetService ratesSheet;
    private final BybitService bybitService;

    public void updateSpreadsheetData() {
        List<String> rates = new ArrayList<>();

        rates.add(getJ3Rate());
        rates.add(getJ4Rate());

        ratesSheet.updateRates(rates);
    }

    private String getJ3Rate() {
        BybitAd ad = bybitService.getSingleAd(BybitToken.USDT,
                BybitFiat.RUB,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.BANK_TRANSFER),
                10_000,
                8);
        double rate = ad.getPrice() / 1.00275;
        return formatDoubleToCommaString(rate);
    }

    private String getJ4Rate() {
        BybitAd ad = bybitService.getSingleAd(BybitToken.USDT,
                BybitFiat.RUB,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.BANK_TRANSFER),
                10_000,
                20);

        double invertedPrice = 1 / ad.getPrice();

        BigDecimal decimal = new BigDecimal(invertedPrice)
                .setScale(4, RoundingMode.DOWN);

        return formatDecimalToCommaString(decimal);
    }

    private String formatDoubleToCommaString(double value) {
        return String.valueOf(value).replace('.', ',');
    }

    private String formatDecimalToCommaString(BigDecimal value) {
        return String.valueOf(value).replace('.', ',');
    }

}
