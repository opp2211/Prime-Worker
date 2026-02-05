package ru.maltsev.primeworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.dto.BybitAd;
import ru.maltsev.primeworker.model.BybitFiat;
import ru.maltsev.primeworker.model.BybitPaymentMethod;
import ru.maltsev.primeworker.model.BybitSide;
import ru.maltsev.primeworker.model.BybitToken;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrimeService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Yekaterinburg");

    private final GoogleSheetsService googleSheetsService;
    private final BybitService bybitService;

    public void updateSpreadsheetData() {
        updateBybitRates();
    }

    public void updateBybitRates() {
        updateRub8thRate();
        updateRub20thRate();
        updateKzt1stRate();
        updateKzt2ndRate();
    }

    private void updateRub8thRate() {
        String startCellRef = "Курс!F5";

        BybitAd ad = bybitService.getSingleAd(
                BybitToken.USDT,
                BybitFiat.RUB,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.BANK_TRANSFER),
                10_000,
                8);

        writeTimestampAndRate(startCellRef, ad.getPrice());
    }

    private void updateRub20thRate() {
        String startCellRef = "Курс!F6";

        BybitAd ad = bybitService.getSingleAd(
                BybitToken.USDT,
                BybitFiat.RUB,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.BANK_TRANSFER),
                10_000,
                20);

        writeTimestampAndRate(startCellRef, ad.getPrice());
    }

    private void updateKzt1stRate() {
        String startCellRef = "Курс!F7";

        BybitAd ad = bybitService.getSingleAd(
                BybitToken.USDT,
                BybitFiat.KZT,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.KASPI_BANK),
                50_000,
                1);

        writeTimestampAndRate(startCellRef, ad.getPrice());
    }

    private void updateKzt2ndRate() {
        String startCellRef = "Курс!F8";

        BybitAd ad = bybitService.getSingleAd(
                BybitToken.USDT,
                BybitFiat.KZT,
                BybitSide.BUY,
                List.of(BybitPaymentMethod.KASPI_BANK),
                50_000,
                2);

        writeTimestampAndRate(startCellRef, ad.getPrice());
    }

    private void writeTimestampAndRate(String startCellRef, Double rate) {
        String timestamp = ZonedDateTime.now(ZONE_ID).format(DTF);
        List<Object> dataList = List.of(timestamp, formatDoubleToCommaString(rate));
        try {
            googleSheetsService.writeDataToRow(dataList, startCellRef);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String formatDoubleToCommaString(double value) {
        return String.valueOf(value).replace('.', ',');
    }

}
