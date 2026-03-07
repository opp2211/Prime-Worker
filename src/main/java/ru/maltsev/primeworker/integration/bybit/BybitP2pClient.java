package ru.maltsev.primeworker.integration.bybit;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.maltsev.primeworker.config.properties.BybitProps;
import ru.maltsev.primeworker.domain.p2p.P2pAd;
import ru.maltsev.primeworker.domain.p2p.P2pQuery;
import ru.maltsev.primeworker.domain.p2p.TradeSide;
import ru.maltsev.primeworker.integration.bybit.dto.BybitAd;
import ru.maltsev.primeworker.integration.bybit.dto.BybitResponse;
import ru.maltsev.primeworker.integration.bybit.dto.BybitServerTimeResponse;
import ru.maltsev.primeworker.integration.p2p.P2pClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BybitP2pClient implements P2pClient {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final WebClient bybitWebClient;
    private final BybitProps props;
    private final Gson gson;

    @Override
    public List<P2pAd> findAds(P2pQuery query) throws BybitApiException {
        Map<String, Object> params = new HashMap<>();

        int page = query.resolvePage(DEFAULT_PAGE_NUM);
        int pageSize = query.resolvePageSize(DEFAULT_PAGE_SIZE);

        params.put("tokenId", query.asset().getCode());
        params.put("currencyId", query.fiat().getCode());
        params.put("side", resolveBybitSide(query.side()));
        params.put("page", Integer.toString(page));
        params.put("size", Integer.toString(pageSize));

        if (!query.paymentMethods().isEmpty()) {
            params.put("payment", query.paymentMethods());
        }
        if (query.amount() != null) {
            params.put("amount", query.amount().toPlainString());
        }

        String jsonBody = gson.toJson(params);
        String timestamp = String.valueOf(getServerSyncedTimestampMillis());
        String signature = generateSignature(jsonBody, timestamp);

        log.info("Bybit P2P request: token={}, fiat={}, side={}, page={}, size={}, amount={}, payments={}",
                query.asset(), query.fiat(), query.side(), page, pageSize, query.amount(), query.paymentMethods());
        log.debug("Bybit P2P request body: {}", jsonBody);

        BybitResponse response =
                bybitWebClient.post()
                        .uri("/v5/p2p/item/online")
                        .header("X-BAPI-API-KEY", props.getApiKey())
                        .header("X-BAPI-TIMESTAMP", timestamp)
                        .header("X-BAPI-SIGN", signature)
                        .header("X-BAPI-RECV-WINDOW", props.getRecvWindow())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(jsonBody)
                        .retrieve()
                        .bodyToMono(BybitResponse.class)
                        .block();

        if (response == null) {
            log.error("Bybit response is null");
            throw new BybitApiException("Empty response");
        }

        int itemsCount = (response.getResult() == null || response.getResult().getItems() == null)
                ? -1
                : response.getResult().getItems().size();
        log.info("Bybit response: retCode={}, retMsg={}, itemsCount={}",
                response.getRetCode(), response.getRetMsg(), itemsCount);
        log.debug("Bybit full response: {}", response);

        if (response.getRetCode() != 0) {
            log.warn("Bybit error response: retCode={}, retMsg={}", response.getRetCode(), response.getRetMsg());
            throw new BybitApiException(response.getRetMsg());
        }

        if (response.getResult() == null || response.getResult().getItems() == null) {
            return List.of();
        }

        return response.getResult().getItems()
                .stream()
                .map(this::mapAd)
                .toList();
    }

    private long getServerSyncedTimestampMillis() {
        long localTime = Instant.now().toEpochMilli();
        try {
            BybitServerTimeResponse timeResponse = bybitWebClient.get()
                    .uri("/v5/market/time")
                    .retrieve()
                    .bodyToMono(BybitServerTimeResponse.class)
                    .block();

            if (timeResponse == null) {
                log.warn("Bybit time response is null, using local time: {}", localTime);
                return localTime;
            }

            if (timeResponse.getRetCode() != 0) {
                log.warn("Bybit time error: retCode={}, retMsg={}, using local time: {}",
                        timeResponse.getRetCode(), timeResponse.getRetMsg(), localTime);
                return localTime;
            }

            long serverTime = resolveServerTimeMillis(timeResponse);
            if (serverTime <= 0) {
                log.warn("Bybit time missing in response, using local time: {}", localTime);
                return localTime;
            }

            long skew = serverTime - localTime;
            log.info("Bybit server time: {} (local {}, skew {} ms)", serverTime, localTime, skew);
            return serverTime;

        } catch (Exception e) {
            log.warn("Failed to fetch Bybit server time, using local time: {}", localTime, e);
            return localTime;
        }
    }

    private long resolveServerTimeMillis(BybitServerTimeResponse timeResponse) {
        if (timeResponse.getTime() != null && timeResponse.getTime() > 0) {
            return timeResponse.getTime();
        }
        if (timeResponse.getResult() != null) {
            String timeNano = timeResponse.getResult().getTimeNano();
            if (timeNano != null && !timeNano.isBlank()) {
                return Long.parseLong(timeNano) / 1_000_000L;
            }
            String timeSecond = timeResponse.getResult().getTimeSecond();
            if (timeSecond != null && !timeSecond.isBlank()) {
                return Long.parseLong(timeSecond) * 1_000L;
            }
        }
        return -1L;
    }

    private P2pAd mapAd(BybitAd ad) {
        return new P2pAd(
                ad.getNickName(),
                ad.getPrice(),
                ad.getQuantity(),
                ad.getMinAmount(),
                ad.getMaxAmount(),
                ad.getPayments(),
                ad.getRemark()
        );
    }

    private String resolveBybitSide(TradeSide side) {
        return switch (side) {
            case BUY -> "1";
            case SELL -> "0";
        };
    }

    private String generateSignature(String jsonBody, String timestamp) {
        try {
            String payload = timestamp
                    + props.getApiKey()
                    + props.getRecvWindow()
                    + jsonBody;

            Mac sha256Hmac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    props.getApiSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            sha256Hmac.init(secretKey);

            return bytesToHex(
                    sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8))
            );

        } catch (Exception e) {
            throw new RuntimeException("Signature error", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
