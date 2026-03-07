package ru.maltsev.primeworker.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.maltsev.primeworker.service.RatesUpdateService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RatesTaskController {

    private final RatesUpdateService ratesUpdateService;

    @PostMapping("/prime-task/run")
    public Mono<ResponseEntity<String>> run() {
        return Mono.fromRunnable(ratesUpdateService::updateRatesSheet)
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("Started"));
    }
}
