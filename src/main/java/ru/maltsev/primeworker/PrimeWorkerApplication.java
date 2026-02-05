package ru.maltsev.primeworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrimeWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrimeWorkerApplication.class, args);
    }

}
