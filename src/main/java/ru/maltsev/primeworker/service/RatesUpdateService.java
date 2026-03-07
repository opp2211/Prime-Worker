package ru.maltsev.primeworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.domain.rate.RatesSnapshot;
import ru.maltsev.primeworker.sheet.RatesSheetWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatesUpdateService {

    private final RatesCalculationService calculationService;
    private final RatesSheetWriter sheetWriter;

    public void updateRatesSheet() {
        RatesSnapshot snapshot = calculationService.calculateRates();
        sheetWriter.updateRates(snapshot);
        log.info("Rates updated: {} values", snapshot.values().size());
    }
}
