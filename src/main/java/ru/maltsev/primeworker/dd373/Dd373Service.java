package ru.maltsev.primeworker.dd373;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.maltsev.primeworker.dd373.dto.Dd373PriceDto;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Dd373Service {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36";

    private static final String MERCHANTS_URL =
            "https://www.dd373.com/s-3hcpqw-bwgvrk-fj6p5a-0-0-0-8rknmp-0-0-receive-0-0-1-0-0-1.html";

    private static final String SELLERS_URL =
            "https://www.dd373.com/s-3hcpqw-bwgvrk-fj6p5a-0-0-0-8rknmp-0-0-0-0-0-1-0-0-1.html?qufu=true";

    private static final Pattern DD373_ARG1_PATTERN = Pattern.compile("var\\s+arg1='([0-9A-F]+)'");

    private static final Pattern MERCHANT_PRICE_PATTERN = Pattern.compile(
            "([0-9]+(?:\\.[0-9]+)?)\\s*\\u5143\\s*/\\s*\\u4e2a(?:\\u795e\\u5723\\u77f3)?"
    );

    private static final Pattern SELLER_PRICE_PATTERN = Pattern.compile(
            "1\\s*\\u4e2a(?:\\u795e\\u5723\\u77f3)?\\s*=\\s*([0-9]+(?:\\.[0-9]+)?)\\s*\\u5143"
    );

    private static final String ACW_SC_V2_XOR_KEY = "3000176000856006061501533003690027800375";

    private static final int[] ACW_SC_V2_PERMUTATION = {
            15, 35, 29, 24, 33, 16, 1, 38, 10, 9,
            19, 31, 40, 27, 22, 23, 25, 13, 6, 11,
            39, 18, 20, 8, 14, 21, 32, 26, 2, 30,
            7, 4, 17, 5, 3, 28, 34, 37, 12, 36
    };

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
        Connection.Response firstResponse = newConnection(url).execute();
        if (!isAntiBotChallenge(firstResponse.body())) {
            return firstResponse.parse();
        }

        Map<String, String> cookies = new HashMap<>(firstResponse.cookies());
        cookies.put("acw_sc__v2", buildAcwScV2(extractArg1(firstResponse.body())));

        Connection.Response secondResponse = newConnection(url)
                .cookies(cookies)
                .execute();

        if (isAntiBotChallenge(secondResponse.body())) {
            throw new IOException("dd373 anti-bot challenge is still active after cookie retry");
        }

        return secondResponse.parse();
    }

    private Connection newConnection(String url) {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .timeout(15000);
    }

    private boolean isAntiBotChallenge(String body) {
        return body.contains("acw_sc__v2") && DD373_ARG1_PATTERN.matcher(body).find();
    }

    private String extractArg1(String body) throws IOException {
        Matcher matcher = DD373_ARG1_PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new IOException("dd373 anti-bot page does not contain arg1");
        }
        return matcher.group(1);
    }

    static String buildAcwScV2(String arg1) {
        if (arg1 == null || arg1.length() != ACW_SC_V2_PERMUTATION.length) {
            throw new IllegalArgumentException("Unexpected dd373 arg1 length");
        }

        char[] reordered = new char[ACW_SC_V2_PERMUTATION.length];
        for (int i = 0; i < ACW_SC_V2_PERMUTATION.length; i++) {
            reordered[i] = arg1.charAt(ACW_SC_V2_PERMUTATION[i] - 1);
        }

        StringBuilder result = new StringBuilder(ACW_SC_V2_XOR_KEY.length());
        for (int i = 0; i < reordered.length; i += 2) {
            int left = Integer.parseInt(new String(reordered, i, 2), 16);
            int right = Integer.parseInt(ACW_SC_V2_XOR_KEY.substring(i, i + 2), 16);
            int value = left ^ right;
            if (value < 16) {
                result.append('0');
            }
            result.append(Integer.toHexString(value));
        }
        return result.toString();
    }

    private List<Dd373PriceDto> parseMerchantPrices(Document doc) {
        List<Dd373PriceDto> result = new ArrayList<>();

        Elements rows = doc.select(".platform-receive-content ul");
        if (rows.isEmpty()) {
            rows = doc.select(".platform-receive-content");
        }

        for (Element row : rows) {
            Elements paragraphs = row.select("p.font12.color666");

            for (Element p : paragraphs) {
                String text = p.text().trim();
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
                BigDecimal price = parseSellerPrice(text);
                if (price != null) {
                    result.add(new Dd373PriceDto(price, text));
                }
            }
        }

        return result;
    }

    private BigDecimal parseMerchantPrice(String text) {
        Matcher matcher = MERCHANT_PRICE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        try {
            return new BigDecimal(matcher.group(1));
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseSellerPrice(String text) {
        Matcher matcher = SELLER_PRICE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        try {
            return new BigDecimal(matcher.group(1));
        } catch (Exception e) {
            return null;
        }
    }
}
