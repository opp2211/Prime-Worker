package ru.maltsev.primeworker.task;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.service.PrimeService;

@Service
@RequiredArgsConstructor
public class PrimeTask {

    private final PrimeService primeService;

    @Scheduled(cron = "*/30 * * * * *")
    public void doTask() {
        primeService.updateSpreadsheetData();
    }
}
