package ru.maltsev.primeworker.task;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.service.RatesUpdateService;

@Service
@RequiredArgsConstructor
public class RatesUpdateTask {

    private final RatesUpdateService ratesUpdateService;

    @Scheduled(cron = "0 0/20 * * * *")
    public void doTask() {
        ratesUpdateService.updateRatesSheet();
    }
}
