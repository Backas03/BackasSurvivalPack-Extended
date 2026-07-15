package kr.kro.backas.backassurvivalpackextended.coin;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 업비트 KRW 마켓 사전. 한글명/영문명/심볼은 전부 API에서 받아온다 (하드코딩 없음).
 * 신규 상장/상장폐지는 주기 갱신 시 자동 반영된다.
 */
public final class MarketRegistry {

    private static volatile Map<String, UpbitMarket> byCode = Map.of();

    private MarketRegistry() {
    }

    /** 비동기 스레드에서 호출 */
    public static void refresh() throws IOException {
        UpbitMarket[] all = UpbitClient.get("/v1/market/all", UpbitMarket[].class);
        Map<String, UpbitMarket> codeMap = new LinkedHashMap<>();
        for (UpbitMarket market : all) {
            if (market.getMarket() != null && market.getMarket().startsWith("KRW-")) {
                codeMap.put(market.getMarket(), market);
            }
        }
        byCode = Collections.unmodifiableMap(codeMap);
    }

    public static boolean isLoaded() {
        return !byCode.isEmpty();
    }

    public static Collection<UpbitMarket> getAll() {
        return byCode.values();
    }

    @Nullable
    public static UpbitMarket byCode(String marketCode) {
        return byCode.get(marketCode);
    }

    /** "비트코인" / "BTC" / "btc" / "Bitcoin" / "KRW-BTC" -> UpbitMarket */
    @Nullable
    public static UpbitMarket resolve(String input) {
        if (input == null || input.isBlank()) return null;
        String trimmed = input.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);

        UpbitMarket direct = byCode.get(upper);
        if (direct != null) return direct;

        UpbitMarket bySymbol = byCode.get("KRW-" + upper);
        if (bySymbol != null) return bySymbol;

        for (UpbitMarket market : byCode.values()) {
            if (market.getKoreanName().equals(trimmed)) return market;
        }
        for (UpbitMarket market : byCode.values()) {
            if (market.getEnglishName().equalsIgnoreCase(trimmed)) return market;
        }
        return null;
    }

    /** 한글명/영문명/심볼 부분 일치 검색 */
    public static List<UpbitMarket> search(String keyword) {
        List<UpbitMarket> result = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return result;
        String trimmed = keyword.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        String upper = trimmed.toUpperCase(Locale.ROOT);
        for (UpbitMarket market : byCode.values()) {
            if (market.getKoreanName().contains(trimmed)
                    || market.getEnglishName().toLowerCase(Locale.ROOT).contains(lower)
                    || market.getSymbol().contains(upper)) {
                result.add(market);
            }
        }
        return result;
    }
}
