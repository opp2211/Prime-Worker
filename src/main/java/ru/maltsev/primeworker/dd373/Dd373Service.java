package ru.maltsev.primeworker.dd373;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.dd373.dto.Dd373PriceDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class Dd373Service {

    private static final String MERCHANTS_URL =
            "https://www.dd373.com/s-mnh4dv-n75hgf-2a7xrg-0-0-0-94vje2-0-0-recycle-0-0-1-0-0-1.html";

    private static final String SELLERS_URL =
            "https://www.dd373.com/s-mnh4dv-n75hgf-2a7xrg-0-0-0-94vje2-0-0-0-0-0-1-0-0-1.html";

    public List<Dd373PriceDto> getMerchantPrices() {
        try {
            Document doc = loadDocument(MERCHANTS_URL);
            return parseMerchantPrices(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dd373 merchant prices", e);
        }
    }

    public List<Dd373PriceDto> getSellerPrices() {
        try {
            Document doc = loadDocument(SELLERS_URL);
            return parseSellerPrices(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dd373 seller prices", e);
        }
    }

    private Document loadDocument(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .timeout(15000)
                .get();
    }

    private List<Dd373PriceDto> parseMerchantPrices(Document doc) {
        List<Dd373PriceDto> result = new ArrayList<>();

        Elements rows = doc.select(".platform-receive-content ul");

        for (Element row : rows) {
            Elements paragraphs = row.select("p.font12.color666");

            for (Element p : paragraphs) {
                String text = p.text().trim();

                if (!text.contains("元/个神圣石")) {
                    continue;
                }

                BigDecimal price = parseMerchantPrice(text);
                if (price != null) {
                    result.add(new Dd373PriceDto(price, text));
                }
            }
        }

        return result;
    }

    private List<Dd373PriceDto> parseSellerPrices(Document doc) {
        List<Dd373PriceDto> result = new ArrayList<>();

        Elements items = doc.select(".goods-list-item");

        for (Element item : items) {
            Elements paragraphs = item.select(".kucun p.font12.color666");

            for (Element p : paragraphs) {
                String text = p.text().trim();

                if (!text.contains("1个神圣石=") || !text.endsWith("元")) {
                    continue;
                }

                BigDecimal price = parseSellerPrice(text);
                if (price != null) {
                    result.add(new Dd373PriceDto(price, text));
                }
            }
        }

        return result;
    }

    private BigDecimal parseMerchantPrice(String text) {
        String normalized = text
                .replace("元/个神圣石", "")
                .trim();

        try {
            return new BigDecimal(normalized);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseSellerPrice(String text) {
        String normalized = text
                .replace("1个神圣石=", "")
                .replace("元", "")
                .trim();

        try {
            return new BigDecimal(normalized);
        } catch (Exception e) {
            return null;
        }
    }
}