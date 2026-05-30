package ru.maltsev.primeworker.g2g;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.g2g.dto.G2gOfferDto;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class G2gService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private static final String URL =
            "https://sls.g2g.com/offer/search" +
                    "?seo_term=path-of-exile-2-currency" +
                    "&filter_attr=lgc_27013_platform:lgc_27013_platform_62230%7Clgc_27013_tier:lgc_27013_tier_54399" +
                    "&sort=lowest_price" +
                    "&page_size=20" +
                    "&group=0" +
                    "&currency=USD" +
                    "&country=DE" +
                    "&v=v2";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public List<G2gOfferDto> getOffers() {
        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(REQUEST_TIMEOUT)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("G2G returned HTTP status " + response.statusCode());
            }

            return parseOffers(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch G2G offers", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch G2G offers", e);
        }
    }

    private List<G2gOfferDto> parseOffers(String json) {
        JsonNode root = objectMapper.readTree(json);
        JsonNode results = root.path("payload").path("results");

        List<G2gOfferDto> offers = new ArrayList<>();

        for (JsonNode node : results) {
            String username = node.get("username") != null ? node.get("username").asString() : null;
            String priceText = node.get("display_price") != null ? node.get("display_price").asString() : null;

            offers.add(new G2gOfferDto(
                    username,
                    priceText != null ? new BigDecimal(priceText) : null
            ));
        }

        return offers;
    }
}
