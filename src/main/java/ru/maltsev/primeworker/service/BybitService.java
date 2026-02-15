package ru.maltsev.primeworker.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.maltsev.primeworker.config.properties.BybitProps;
import ru.maltsev.primeworker.dto.BybitAd;
import ru.maltsev.primeworker.dto.BybitResponse;
import ru.maltsev.primeworker.dto.BybitServerTimeResponse;
import ru.maltsev.primeworker.exception.BybitApiException;
import ru.maltsev.primeworker.model.BybitFiat;
import ru.maltsev.primeworker.model.BybitPaymentMethod;
import ru.maltsev.primeworker.model.BybitSide;
import ru.maltsev.primeworker.model.BybitToken;

import javax.annotation.Nullable;
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
public class BybitService {

    private final int DEFAULT_PAGE_NUM = 1;
    private final int DEFAULT_PAGE_SIZE = 10;

    private final WebClient bybitWebClient;
    private final BybitProps props;
    private final Gson gson;

    public BybitAd getSingleAd(BybitToken bybitToken,
                              BybitFiat bybitFiat,
                              BybitSide bybitSide,
                              @Nullable List<BybitPaymentMethod> paymentMethods,
                              @Nullable Integer amount,
                              int adIndex) {
        return getAds(bybitToken, bybitFiat, bybitSide, paymentMethods, amount, adIndex, 1).getFirst();
    }

    public List<BybitAd> getAds(BybitToken bybitToken,
                                BybitFiat bybitFiat,
                                BybitSide bybitSide,
                                @Nullable List<BybitPaymentMethod> paymentMethods,
                                @Nullable Integer amount) {
        return getAds(bybitToken, bybitFiat, bybitSide, paymentMethods, amount, DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE);
    }

    public List<BybitAd> getAds(BybitToken bybitToken,
                                BybitFiat bybitFiat,
                                BybitSide bybitSide,
                                @Nullable List<BybitPaymentMethod> paymentMethods,
                                @Nullable Integer amount,
                                Integer page,
                                Integer pageSize) throws BybitApiException {

        Map<String, Object> params = new HashMap<>();

        params.put("tokenId", bybitToken.getCode());
        params.put("currencyId", bybitFiat.getCode());
        params.put("side", bybitSide.getCode());
        params.put("page", page.toString());
        params.put("size", pageSize.toString());
        if (paymentMethods != null && !paymentMethods.isEmpty()) {
            List<String> stringList = paymentMethods.stream()
                    .map(BybitPaymentMethod::getCode)
                    .toList();
            params.put("payment", stringList);
        }
        if (amount != null) {
            params.put("amount", amount.toString());
        }

        String jsonBody = gson.toJson(params);
        String timestamp = String.valueOf(getServerSyncedTimestampMillis());
        String signature = generateSignature(jsonBody, timestamp);

        log.info("Bybit P2P request: token={}, fiat={}, side={}, page={}, size={}, amount={}, payments={}",
                bybitToken, bybitFiat, bybitSide, page, pageSize, amount, paymentMethods);
        log.debug("Bybit P2P request body: {}", jsonBody);

        BybitResponse response =
                bybitWebClient.post()
                        .uri("/v5/p2p/item/online")
                        .header("X-BAPI-API-KEY", props.getApiKey())
                        .header("X-BAPI-TIMESTAMP", timestamp)
                        .header("X-BAPI-SIGN", signature)
                        .header("X-BAPI-RECV-WINDOW", props.getRecvWindow())
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

        return response.getResult().getItems();

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
