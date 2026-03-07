package ru.maltsev.primeworker.integration.binance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.maltsev.primeworker.domain.p2p.P2pAd;
import ru.maltsev.primeworker.domain.p2p.P2pQuery;
import ru.maltsev.primeworker.integration.binance.dto.BinanceRequest;
import ru.maltsev.primeworker.integration.binance.dto.BinanceResponse;
import ru.maltsev.primeworker.integration.p2p.P2pClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BinanceP2pClient implements P2pClient {

    private static final String BINANCE_P2P_URL =
            "https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search";
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final RestTemplate restTemplate;

    @Override
    public List<P2pAd> findAds(P2pQuery query) {
        BinanceRequest request = new BinanceRequest();
        request.setAsset(query.asset().getCode());
        request.setFiat(query.fiat().getCode());
        request.setTradeType(query.side().name());
        request.setPage(query.resolvePage(DEFAULT_PAGE_NUM));
        request.setRows(query.resolvePageSize(DEFAULT_PAGE_SIZE));
        if (!query.paymentMethods().isEmpty()) {
            request.setPayTypes(query.paymentMethods());
        }

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

        BinanceResponse body = response.getBody();
        if (body == null || body.getData() == null) {
            return List.of();
        }

        return body.getData()
                .stream()
                .map(this::mapAd)
                .toList();
    }

    private P2pAd mapAd(BinanceResponse.AdvData data) {
        String seller = data.getAdvertiser() == null ? null : data.getAdvertiser().getNickName();
        BigDecimal price = null;
        if (data.getAdv() != null && data.getAdv().getPrice() != null) {
            price = new BigDecimal(data.getAdv().getPrice());
        }
        return new P2pAd(seller, price, null, null, null, List.of(), null);
    }
}
