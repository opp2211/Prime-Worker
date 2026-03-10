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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class G2gService {

    private static final String URL =
            "https://sls.g2g.com/offer/search" +
                    "?seo_term=poe-currency" +
                    "&filter_attr=lgc_19398_tier:lgc_19398_tier_42692%7Clgc_19398_server:lgc_19398_server_61212" +
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
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return parseOffers(response.body());

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
