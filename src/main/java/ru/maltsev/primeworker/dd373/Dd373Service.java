package ru.maltsev.primeworker.dd373;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.dd373.dto.Dd373PriceDto;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class Dd373Service {

    static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36";

    private static final String MERCHANTS_URL =
            "https://goods.dd373.com/Api/Receive/UserCenter/ApiGetNeedShopList"
                    + "?GameId=3e7c2e61c71142b2a330b85a5f6d09b2"
                    + "&GameOtherId=49a82504a8e4490fb8ff8bc8dd8c4a08_84217c5bec5a4e48bc9359b94bc6ae90"
                    + "&GameShopTypeId=a34b2bed8f794b6eac11dce4fa9bb6d7";

    private static final String SELLERS_URL =
            "https://goods.dd373.com/Api/Goods/UserCenter/ApiGetShopList"
                    + "?GameId=3e7c2e61c71142b2a330b85a5f6d09b2"
                    + "&GameOtherId=49a82504a8e4490fb8ff8bc8dd8c4a08_84217c5bec5a4e48bc9359b94bc6ae90"
                    + "&GameShopTypeId=a34b2bed8f794b6eac11dce4fa9bb6d7";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration requestTimeout;

    @Autowired
    public Dd373Service(HttpClient httpClient, ObjectMapper objectMapper) {
        this(httpClient, objectMapper, REQUEST_TIMEOUT);
    }

    Dd373Service(HttpClient httpClient, ObjectMapper objectMapper, Duration requestTimeout) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.requestTimeout = requestTimeout;
    }

    public List<Dd373PriceDto> getMerchantPrices() {
        return fetchPrices(MERCHANTS_URL, "merchant");
    }

    public List<Dd373PriceDto> getSellerPrices() {
        return fetchPrices(SELLERS_URL, "seller");
    }

    private List<Dd373PriceDto> fetchPrices(String url, String priceType) {
        CompletableFuture<HttpResponse<String>> responseFuture = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(requestTimeout)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .header("Referer", "https://www.dd373.com/")
                    .GET()
                    .build();

            responseFuture = httpClient.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            HttpResponse<String> response = responseFuture.get(requestTimeout.toMillis(), TimeUnit.MILLISECONDS);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("DD373 returned HTTP status " + response.statusCode());
            }

            return parsePrices(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cancel(responseFuture);
            throw new RuntimeException("Failed to fetch DD373 " + priceType + " prices", e);
        } catch (TimeoutException e) {
            cancel(responseFuture);
            throw new RuntimeException("Failed to fetch DD373 " + priceType + " prices", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new RuntimeException("Failed to fetch DD373 " + priceType + " prices", cause);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch DD373 " + priceType + " prices", e);
        }
    }

    private void cancel(CompletableFuture<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    List<Dd373PriceDto> parsePrices(String json) {
        JsonNode root = objectMapper.readTree(json);
        validateResponseCode(root, "StatusCode", "StatusMsg", "DD373 status");

        JsonNode statusData = root.get("StatusData");
        if (statusData == null) {
            throw new IllegalStateException("DD373 response does not contain StatusData");
        }
        validateResponseCode(statusData, "ResultCode", "ResultMsg", "DD373 result");

        JsonNode resultData = statusData.get("ResultData");
        if (resultData == null || !resultData.isArray()) {
            throw new IllegalStateException("DD373 response does not contain ResultData array");
        }

        List<Dd373PriceDto> prices = new ArrayList<>();
        for (JsonNode node : resultData) {
            Dd373PriceDto price = mapPrice(node);
            if (price.getSingleprice() != null) {
                prices.add(price);
            }
        }
        return prices;
    }

    private void validateResponseCode(JsonNode node, String codeField, String messageField, String context) {
        String code = stringValue(node, codeField);
        if (!"0".equals(code)) {
            throw new IllegalStateException(context + " is not successful: " + code + " " + stringValue(node, messageField));
        }
    }

    private Dd373PriceDto mapPrice(JsonNode node) {
        Dd373PriceDto dto = new Dd373PriceDto();
        dto.setId(stringValue(node, "id"));
        dto.setShopno(stringValue(node, "shopno"));
        dto.setTrade(stringValue(node, "trade"));
        dto.setNumber(decimalValue(node, "number"));
        dto.setUnit(stringValue(node, "unit"));
        dto.setAmount(decimalValue(node, "amount"));
        dto.setSingleprice(decimalValue(node, "singleprice"));
        dto.setMaxamount(decimalValue(node, "maxamount"));
        dto.setMinamount(decimalValue(node, "minamount"));
        dto.setSinglecount(decimalValue(node, "singlecount"));
        dto.setPrice(decimalValue(node, "price"));
        return dto;
    }

    private String stringValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null) {
            return null;
        }

        String text = value.asString();
        return text == null || text.isBlank() ? null : text;
    }

    private BigDecimal decimalValue(JsonNode node, String fieldName) {
        String value = stringValue(node, fieldName);
        if (value == null) {
            return null;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("DD373 field " + fieldName + " contains invalid decimal value: " + value, e);
        }
    }
}
