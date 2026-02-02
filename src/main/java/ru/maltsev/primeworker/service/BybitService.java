package ru.maltsev.primeworker.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.maltsev.primeworker.config.properties.BybitProps;
import ru.maltsev.primeworker.dto.BybitAd;
import ru.maltsev.primeworker.dto.BybitResponse;
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
public class BybitService {

    private final WebClient bybitWebClient;
    private final BybitProps props;
    private final Gson gson;

    public List<BybitAd> getAds(BybitToken bybitToken,
                                BybitFiat bybitFiat,
                                BybitSide bybitSide,
                                @Nullable List<BybitPaymentMethod> paymentMethods,
                                @Nullable Integer amount) throws BybitApiException {

        System.out.println(props.getApiKey());
        System.out.println(props.getApiSecret());

        Map<String, Object> params = new HashMap<>();

        params.put("tokenId", bybitToken.getCode());
        params.put("currencyId", bybitFiat.getCode());
        params.put("side", bybitSide.getCode());
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

        String timestamp = String.valueOf(Instant.now().toEpochMilli());

        String signature = generateSignature(jsonBody, timestamp);


        System.out.println("Json Body: " + jsonBody);
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Api Key: " + props.getApiKey());
        System.out.println("Api Secret: " + props.getApiSecret());
        System.out.println("Recv Window: " + props.getRecvWindow());

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
            throw new BybitApiException("Empty response");
        }

        if (response.getRetCode() != 0) {
            throw new BybitApiException(response.getRetMsg());
        }

        return response.getResult().getItems();

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
