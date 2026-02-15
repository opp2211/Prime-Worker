package ru.maltsev.primeworker.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.maltsev.primeworker.service.PrimeService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class PrimeTaskController {

    private final PrimeService primeService;

    @PostMapping("/prime-task/run")
    public Mono<ResponseEntity<String>> run() {
        return Mono.fromRunnable(primeService::updateSpreadsheetData)
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("Started"));
    }
}
