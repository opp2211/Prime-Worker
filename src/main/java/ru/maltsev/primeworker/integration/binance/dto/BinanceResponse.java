package ru.maltsev.primeworker.integration.binance.dto;

import lombok.Data;
import java.util.List;

@Data
public class BinanceResponse {

    private List<AdvData> data;

    @Data
    public static class AdvData {
        private Adv adv;
        private Advertiser advertiser;
    }

    @Data
    public static class Adv {
        private String price;
    }

    @Data
    public static class Advertiser {
        private String nickName;
    }
}
