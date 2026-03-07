package ru.maltsev.primeworker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.maltsev.primeworker.dto.BinanceRequest;
import ru.maltsev.primeworker.dto.BinanceResponse;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class BinanceService {

    private final RestTemplate restTemplate;

    private static final String BINANCE_P2P_URL =
            "https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search";

    public BinanceResponse getFifthBuyUsdtToCny() {

        BinanceRequest request = new BinanceRequest();
        request.setAsset("USDT");
        request.setFiat("CNY");
        request.setTradeType("BUY");
        request.setPage(5);
        request.setRows(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.ACCEPT_ENCODING, "identity");

        HttpEntity<BinanceRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<BinanceResponse> response = restTemplate.exchange(
                BINANCE_P2P_URL,
                HttpMethod.POST,
                entity,
                BinanceResponse.class
        );

        return response.getBody();
    }
}