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

        var usdToRubRate = getUsdToRubRate();
        var rubToUsdRate = getRubToUsdRate();

        var kztToUsdRate = getKztToUsdRate();
        var kztToRubRate = getKztToRubRate(kztToUsdRate, usdToRubRate);

        var usdToKztRate = getUsdToKztRate();
        var rubToKztRate = getRubToKztRate(rubToUsdRate, usdToKztRate);

        rates.add(formatDecimalToCommaString(usdToRubRate));
        rates.add(formatDecimalToCommaString(rubToUsdRate));
        rates.add(formatDecimalToCommaString(kztToRubRate));
        rates.add(formatDecimalToCommaString(rubToKztRate));

        ratesSheet.updateRates(rates);
    }

    private BigDecimal getUsdToRubRate() {
        BybitAd ad = bybitService.getSingleAd(BybitToken.USDT,
                BybitFiat.RUB,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.BANK_TRANSFER),
                10_000,
                8);
        double rate = ad.getPrice() / 1.00275;
        return BigDecimal.valueOf(rate)
                .setScale(4, RoundingMode.DOWN);
    }

    private BigDecimal getRubToUsdRate() {
        BybitAd ad = bybitService.getSingleAd(BybitToken.USDT,
                BybitFiat.RUB,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.BANK_TRANSFER),
                10_000,
                20);

        double invertedPrice = 1 / ad.getPrice();
        return BigDecimal.valueOf(invertedPrice)
                .setScale(4, RoundingMode.DOWN);
    }

    private BigDecimal getUsdToKztRate() {
        BybitAd ad = bybitService.getSingleAd(BybitToken.USDT,
                BybitFiat.KZT,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.KASPI_BANK),
                50_000,
                2);

        return BigDecimal.valueOf(ad.getPrice())
                .setScale(2, RoundingMode.DOWN);
    }

    private BigDecimal getKztToUsdRate() {
        BybitAd ad = bybitService.getSingleAd(BybitToken.USDT,
                BybitFiat.KZT,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.KASPI_BANK),
                50_000,
                4);

        return BigDecimal.valueOf(1 / ad.getPrice())
                .setScale(4, RoundingMode.DOWN);
    }

    private BigDecimal getKztToRubRate(BigDecimal kztToUsdRate, BigDecimal usdToRubRate) {
        return kztToUsdRate.multiply(usdToRubRate);
    }

    private BigDecimal getRubToKztRate(BigDecimal rubToUsdRate, BigDecimal usdToKztRate) {
        return rubToUsdRate.multiply(usdToKztRate);
    }

    private String formatDecimalToCommaString(BigDecimal value) {
        return String.valueOf(value).replace('.', ',');
    }

}
