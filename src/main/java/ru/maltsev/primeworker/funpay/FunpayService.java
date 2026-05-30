package ru.maltsev.primeworker.funpay;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.funpay.dto.FunpayOfferDto;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FunpayService {

    private static final String URL = "https://funpay.com/chips/173/";



    public List<FunpayOfferDto> getOffers(String league, boolean onlineOnly) {
        return getOffers(league, null, onlineOnly);
    }

    public List<FunpayOfferDto> getOffers(String league, String side, boolean onlineOnly) {
        List<FunpayOfferDto> offers = loadOffers();

        if (league != null && !league.isBlank()) {
            offers = offers.stream()
                    .filter(o -> league.equalsIgnoreCase(o.getLeague()))
                    .collect(Collectors.toList());
        }

        if (side != null && !side.isBlank()) {
            offers = offers.stream()
                    .filter(o -> side.equalsIgnoreCase(o.getSide()))
                    .collect(Collectors.toList());
        }

        if (onlineOnly) {
            offers = offers.stream()
                    .filter(FunpayOfferDto::isOnline)
                    .collect(Collectors.toList());
        }

        offers.sort(Comparator.comparing(
                FunpayOfferDto::getPriceRub,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        return offers;
    }

    private List<FunpayOfferDto> loadOffers() {
        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0")
                    .header("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.8")
                    .cookies(Map.of(
                            "cy", "rub",
                            "locale", "ru"
                    ))
                    .timeout(15000)
                    .get();

            return parseOffers(doc);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить офферы с FunPay", e);
        }
    }

    private List<FunpayOfferDto> parseOffers(Document doc) {
        List<FunpayOfferDto> result = new ArrayList<>();

        Elements rows = doc.select(".tc-item");

        for (Element row : rows) {
            String league = textOrEmpty(row.selectFirst(".tc-server"));
            if (league.isBlank()) {
                league = textOrEmpty(row.selectFirst(".tc-server-inside"));
            }

            String side = textOrEmpty(row.selectFirst(".tc-side"));
            if (side.isBlank()) {
                side = textOrEmpty(row.selectFirst(".tc-side-inside"));
            }

            String seller = textOrEmpty(row.selectFirst(".media-user-name"));

            Element userBlock = row.selectFirst(".media-user");
            boolean online = userBlock != null && userBlock.hasClass("online");

            String stock = textOrEmpty(row.selectFirst(".tc-amount"));

            String priceText = extractRubPrice(row.selectFirst(".tc-price"));
            BigDecimal priceRub = parseRubPrice(priceText);

            result.add(new FunpayOfferDto(
                    league,
                    side,
                    seller,
                    online,
                    stock,
                    priceText,
                    priceRub
            ));
        }

        return result;
    }

    private String textOrEmpty(Element element) {
        return element == null ? "" : element.text().trim();
    }

    private String extractRubPrice(Element priceTd) {
        for (Element child : priceTd.children()) {
            String text = child.text().trim();
            if (text.contains("₽")) {
                return text;
            }
        }

        String allText = priceTd.text().trim();
        String[] lines = allText.split("\\R");
        for (String line : lines) {
            if (line.contains("₽")) {
                return line.trim();
            }
        }

        return allText;
    }

    private BigDecimal parseRubPrice(String priceText) {
        if (priceText == null || !priceText.contains("₽")) {
            return null;
        }

        String normalized = priceText
                .replace("₽", "")
                .replace(" ", "")
                .replace(",", ".")
                .trim();

        try {
            return new BigDecimal(normalized);
        } catch (Exception e) {
            return null;
        }
    }
}
