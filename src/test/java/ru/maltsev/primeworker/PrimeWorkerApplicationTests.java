package ru.maltsev.primeworker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.maltsev.primeworker.config.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class PrimeWorkerApplicationTests {

    @Test
    void contextLoads() {
    }

}
