package kr.kro.backas.backassurvivalpackextended.stock;

import kr.kro.backas.backassurvivalpackextended.util.JsonHttpClient;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 종목 사전. 이름/코드는 전부 외부 소스에서 받아온다:
 * - 코스피/코스닥: 네이버 시가총액순 목록 API (삽입 순서 = 시총순)
 * - 나스닥: nasdaqtrader.com 공식 상장 목록 파일 (심볼순)
 */
public final class StockRegistry {

    private static final int DOMESTIC_PAGE_SIZE = 100;
    private static final int DOMESTIC_MAX_PAGES = 30;

    // 나스닥은 시총순 정렬 소스가 없어 기본 목록만 유명 종목 큐레이션 (이름은 상장 파일에서)
    private static final List<String> NASDAQ_FEATURED = List.of(
            "AAPL", "MSFT", "NVDA", "GOOGL", "AMZN", "META", "TSLA", "AVGO", "NFLX",
            "AMD", "INTC", "QCOM", "CSCO", "ADBE", "PEP", "COST", "TXN", "AMAT",
            "MU", "PLTR", "COIN", "MSTR", "ARM", "PYPL", "ABNB", "LULU", "SBUX",
            "BKNG", "ISRG", "GILD", "PANW", "CRWD", "DDOG", "MRVL", "LRCX", "KLAC"
    );

    private static volatile Map<StockMarket, Map<String, Stock>> byMarket = new EnumMap<>(StockMarket.class);

    private StockRegistry() {
    }

    /** 비동기 스레드에서 호출 */
    public static void refresh() throws IOException, InterruptedException {
        Map<StockMarket, Map<String, Stock>> loaded = new EnumMap<>(StockMarket.class);
        loaded.put(StockMarket.KOSPI, loadDomestic(StockMarket.KOSPI));
        loaded.put(StockMarket.KOSDAQ, loadDomestic(StockMarket.KOSDAQ));
        loaded.put(StockMarket.NASDAQ, loadNasdaq());
        byMarket = loaded;
    }

    private static Map<String, Stock> loadDomestic(StockMarket market) throws IOException, InterruptedException {
        Map<String, Stock> stocks = new LinkedHashMap<>();
        for (int page = 1; page <= DOMESTIC_MAX_PAGES; page++) {
            NaverStockListResponse response = JsonHttpClient.get(
                    "https://m.stock.naver.com/api/stocks/marketValue/" + market.name()
                            + "?page=" + page + "&pageSize=" + DOMESTIC_PAGE_SIZE,
                    NaverStockListResponse.class);
            if (response == null || response.stocks == null || response.stocks.isEmpty()) break;
            for (NaverListItem item : response.stocks) {
                if (item.itemCode != null && item.stockName != null) {
                    stocks.put(item.itemCode, new Stock(market, item.itemCode, item.stockName));
                }
            }
            if ((long) page * DOMESTIC_PAGE_SIZE >= response.totalCount) break;
            Thread.sleep(100); // 페이지 연속 요청 간격
        }
        return Collections.unmodifiableMap(stocks);
    }

    private static Map<String, Stock> loadNasdaq() throws IOException {
        String raw = JsonHttpClient.getRaw("https://www.nasdaqtrader.com/dynamic/symdir/nasdaqlisted.txt");
        Map<String, Stock> stocks = new LinkedHashMap<>();
        for (String line : raw.split("\n")) {
            String[] parts = line.split("\\|");
            if (parts.length < 7) continue;
            String symbol = parts[0].trim();
            String name = parts[1].trim();
            if (symbol.isEmpty() || symbol.equals("Symbol") || symbol.startsWith("File Creation")) continue;
            if (parts[3].trim().equals("Y")) continue; // 테스트 종목 제외
            if (parts[6].trim().equals("Y")) continue; // ETF 제외
            // "Apple Inc. - Common Stock" -> "Apple Inc."
            int suffix = name.indexOf(" - ");
            if (suffix > 0) name = name.substring(0, suffix);
            stocks.put(symbol, new Stock(StockMarket.NASDAQ, symbol, name));
        }
        return Collections.unmodifiableMap(stocks);
    }

    public static boolean isLoaded() {
        Map<String, Stock> kospi = byMarket.get(StockMarket.KOSPI);
        return kospi != null && !kospi.isEmpty();
    }

    public static Collection<Stock> getAll(StockMarket market) {
        Map<String, Stock> stocks = byMarket.get(market);
        return stocks == null ? List.of() : stocks.values();
    }

    /** GUI 기본 목록: 국내 = 시총순 전체, 나스닥 = 유명 종목 큐레이션 */
    public static List<Stock> getFeatured(StockMarket market) {
        if (market != StockMarket.NASDAQ) {
            return new ArrayList<>(getAll(market));
        }
        Map<String, Stock> nasdaq = byMarket.get(StockMarket.NASDAQ);
        List<Stock> featured = new ArrayList<>();
        if (nasdaq == null) return featured;
        for (String symbol : NASDAQ_FEATURED) {
            Stock stock = nasdaq.get(symbol);
            if (stock != null) featured.add(stock);
        }
        return featured;
    }

    @Nullable
    public static Stock byCode(StockMarket market, String code) {
        Map<String, Stock> stocks = byMarket.get(market);
        return stocks == null ? null : stocks.get(code);
    }

    /** "삼성전자" / "005930" / "AAPL" / "Apple Inc." -> Stock (모든 시장에서 탐색) */
    @Nullable
    public static Stock resolve(String input) {
        if (input == null || input.isBlank()) return null;
        String trimmed = input.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);

        for (StockMarket market : StockMarket.values()) {
            Stock byCode = byCode(market, market == StockMarket.NASDAQ ? upper : trimmed);
            if (byCode != null) return byCode;
        }
        for (StockMarket market : StockMarket.values()) {
            for (Stock stock : getAll(market)) {
                if (stock.name().equalsIgnoreCase(trimmed)) return stock;
            }
        }
        return null;
    }

    /** 이름/코드 부분 일치 검색 (특정 시장) */
    public static List<Stock> search(StockMarket market, String keyword) {
        List<Stock> result = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return result;
        String trimmed = keyword.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        String upper = trimmed.toUpperCase(Locale.ROOT);
        for (Stock stock : getAll(market)) {
            if (stock.name().toLowerCase(Locale.ROOT).contains(lower)
                    || stock.code().toUpperCase(Locale.ROOT).contains(upper)) {
                result.add(stock);
            }
        }
        return result;
    }

    // ---- 네이버 목록 응답 모델 ----

    private static class NaverStockListResponse {
        int totalCount;
        List<NaverListItem> stocks;
    }

    private static class NaverListItem {
        String itemCode;
        String stockName;
    }
}
