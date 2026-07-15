package kr.kro.backas.backassurvivalpackextended.stock;

import com.google.gson.reflect.TypeToken;
import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.coin.CoinService;
import kr.kro.backas.backassurvivalpackextended.util.JsonHttpClient;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 주식 시세 조회/캐시.
 * - 코스피/코스닥: 네이버 polling API (배치 조회, 실시간)
 * - 나스닥: 야후 spark(목록용 배치) + chart(상세)
 */
public class StockQuoteService {

    private static final long CACHE_TTL = 15_000;
    private static final int DOMESTIC_BATCH_SIZE = 30;
    private static final long LIVE_INTERVAL = 20L * 7; // 네이버 권장 폴링 주기 7초
    private static final long REGISTRY_REFRESH_INTERVAL = 20L * 60 * 60 * 24;

    private static final Map<String, StockQuote> CACHE = new ConcurrentHashMap<>();
    private final Map<UUID, Stock> liveSubscriptions = new ConcurrentHashMap<>();
    private long lastErrorLog = 0;

    public void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
            try {
                StockRegistry.refresh();
            } catch (Exception e) {
                logThrottled("주식 종목 목록 갱신 실패: " + e.getMessage());
            }
        }, 0L, REGISTRY_REFRESH_INTERVAL);

        Bukkit.getScheduler().runTaskTimerAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
            if (liveSubscriptions.isEmpty()) return;
            try {
                fetchQuotes(new ArrayList<>(liveSubscriptions.values()));
                sendLiveActionBars();
            } catch (Exception e) {
                logThrottled("주식 LIVE 시세 갱신 실패: " + e.getMessage());
            }
        }, LIVE_INTERVAL, LIVE_INTERVAL);
    }

    private void sendLiveActionBars() {
        for (Map.Entry<UUID, Stock> entry : liveSubscriptions.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;
            Stock stock = entry.getValue();
            StockQuote quote = getCached(stock);
            if (quote == null) continue;
            player.sendActionBar(Component.text().append(
                    Component.text("[LIVE] ", TextColor.color(0x86E57F)),
                    Component.text(stock.name() + " ", Palette.GOLD),
                    Component.text(stock.market().formatMoney(CoinService.formatPrice(quote.price())) + " ", Palette.WHITE),
                    Component.text(arrow(quote.changeRate()) + String.format("%.2f%%", Math.abs(quote.changeRate())),
                            color(quote.changeRate()))
            ).build());
        }
    }

    public void toggleLive(Player player, Stock stock) {
        Stock current = liveSubscriptions.get(player.getUniqueId());
        if (current != null && current.key().equals(stock.key())) {
            liveSubscriptions.remove(player.getUniqueId());
            player.sendMessage(Component.text().append(
                    Component.text("[주식] ", Palette.GOLD),
                    Component.text(stock.name(), Palette.WHITE),
                    Component.text(" LIVE 알림을 껐습니다.", Palette.GRAY)
            ));
        } else {
            liveSubscriptions.put(player.getUniqueId(), stock);
            player.sendMessage(Component.text().append(
                    Component.text("[주식] ", Palette.GOLD),
                    Component.text(stock.name() + " (" + stock.code() + ")", Palette.WHITE),
                    Component.text(" LIVE 알림을 켰습니다. 액션바에 실시간 시세가 표시됩니다.", Palette.GRAY)
            ));
        }
    }

    public static StockQuote getCached(Stock stock) {
        return CACHE.get(stock.key());
    }

    /** 캐시가 오래된 종목만 실제 조회한다. 비동기 스레드에서 호출할 것. */
    public static void fetchQuotes(List<Stock> stocks) throws IOException {
        List<Stock> domestic = new ArrayList<>();
        List<Stock> nasdaq = new ArrayList<>();
        for (Stock stock : stocks) {
            StockQuote cached = CACHE.get(stock.key());
            if (cached != null && cached.isFresh(CACHE_TTL)) continue;
            if (stock.market().isDomestic()) domestic.add(stock);
            else nasdaq.add(stock);
        }
        for (int i = 0; i < domestic.size(); i += DOMESTIC_BATCH_SIZE) {
            fetchDomestic(domestic.subList(i, Math.min(i + DOMESTIC_BATCH_SIZE, domestic.size())));
        }
        if (!nasdaq.isEmpty()) {
            fetchNasdaqSpark(nasdaq);
        }
    }

    /** 상세 조회 (나스닥은 고가/저가/거래량 포함 chart 사용). 비동기에서 호출. */
    public static void fetchDetail(Stock stock) throws IOException {
        if (stock.market().isDomestic()) {
            fetchDomestic(List.of(stock));
        } else {
            fetchNasdaqChart(stock);
        }
    }

    private static void fetchDomestic(List<Stock> stocks) throws IOException {
        if (stocks.isEmpty()) return;
        StringBuilder codes = new StringBuilder();
        for (Stock stock : stocks) {
            if (codes.length() > 0) codes.append(',');
            codes.append(stock.code());
        }
        NaverPollingResponse response = JsonHttpClient.get(
                "https://polling.finance.naver.com/api/realtime/domestic/stock/" + codes,
                NaverPollingResponse.class);
        if (response == null || response.datas == null) return;
        long now = System.currentTimeMillis();
        for (NaverStockData data : response.datas) {
            if (data.itemCode == null) continue;
            double price = parse(data.closePriceRaw);
            double changeAmount = Math.abs(parse(data.compareToPreviousClosePriceRaw));
            double changeRate = Math.abs(parse(data.fluctuationsRatioRaw));
            String direction = data.compareToPreviousPrice == null ? "" : data.compareToPreviousPrice.name;
            if (direction != null && direction.toUpperCase(Locale.ROOT).contains("FALL")) {
                changeAmount = -changeAmount;
                changeRate = -changeRate;
            } else if (direction == null || !direction.toUpperCase(Locale.ROOT).contains("RIS")) {
                changeAmount = 0;
                changeRate = 0;
            }
            Stock stock = stocks.stream()
                    .filter(s -> s.code().equals(data.itemCode))
                    .findFirst().orElse(null);
            if (stock == null) continue;
            CACHE.put(stock.key(), new StockQuote(
                    price, changeRate, changeAmount,
                    parse(data.highPriceRaw), parse(data.lowPriceRaw),
                    parse(data.accumulatedTradingValueRaw), parse(data.accumulatedTradingVolumeRaw),
                    "OPEN".equalsIgnoreCase(data.marketStatus), now
            ));
        }
    }

    private static void fetchNasdaqSpark(List<Stock> stocks) throws IOException {
        StringBuilder symbols = new StringBuilder();
        for (Stock stock : stocks) {
            if (symbols.length() > 0) symbols.append(',');
            symbols.append(stock.code());
        }
        Map<String, SparkEntry> response = JsonHttpClient.get(
                "https://query1.finance.yahoo.com/v8/finance/spark?symbols=" + symbols + "&range=1d&interval=1d",
                new TypeToken<Map<String, SparkEntry>>() {
                }.getType());
        if (response == null) return;
        long now = System.currentTimeMillis();
        for (Stock stock : stocks) {
            SparkEntry entry = response.get(stock.code());
            if (entry == null || entry.close == null || entry.close.length == 0) continue;
            double price = entry.close[entry.close.length - 1];
            double prev = entry.chartPreviousClose;
            double changeAmount = prev > 0 ? price - prev : 0;
            double changeRate = prev > 0 ? (price - prev) / prev * 100 : 0;
            CACHE.put(stock.key(), new StockQuote(
                    price, changeRate, changeAmount,
                    -1, -1, -1, -1, null, now
            ));
        }
    }

    private static void fetchNasdaqChart(Stock stock) throws IOException {
        YahooChartResponse response = JsonHttpClient.get(
                "https://query1.finance.yahoo.com/v8/finance/chart/" + stock.code() + "?range=1d&interval=1d",
                YahooChartResponse.class);
        if (response == null || response.chart == null
                || response.chart.result == null || response.chart.result.isEmpty()) return;
        YahooMeta meta = response.chart.result.get(0).meta;
        if (meta == null) return;
        double prev = meta.chartPreviousClose;
        double changeAmount = prev > 0 ? meta.regularMarketPrice - prev : 0;
        double changeRate = prev > 0 ? changeAmount / prev * 100 : 0;
        CACHE.put(stock.key(), new StockQuote(
                meta.regularMarketPrice, changeRate, changeAmount,
                meta.regularMarketDayHigh == null ? -1 : meta.regularMarketDayHigh,
                meta.regularMarketDayLow == null ? -1 : meta.regularMarketDayLow,
                -1,
                meta.regularMarketVolume == null ? -1 : meta.regularMarketVolume,
                null, System.currentTimeMillis()
        ));
    }

    /** 채팅으로 상세 시세 표시 (호출 스레드 무관) */
    public static void sendDetail(Player player, Stock stock) {
        StockQuote quote = getCached(stock);
        if (quote == null) {
            player.sendMessage(Component.text("시세 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.", Palette.RED));
            return;
        }
        StockMarket market = stock.market();
        player.sendMessage(Component.text().append(
                Component.text("[주식] ", Palette.GOLD),
                Component.text(stock.name(), Palette.WHITE),
                Component.text(" (" + market.getDisplayName() + " · " + stock.code() + ")", Palette.GRAY),
                quote.marketOpen() == null ? Component.empty()
                        : Component.text(quote.marketOpen() ? " [장중]" : " [장마감]",
                        quote.marketOpen() ? Palette.GREEN : Palette.GRAY)
        ));
        player.sendMessage(Component.text().append(
                Component.text("현재가: ", Palette.GRAY),
                Component.text(market.formatMoney(CoinService.formatPrice(quote.price())) + " ", Palette.WHITE),
                Component.text(arrow(quote.changeRate())
                        + String.format("%.2f%%", Math.abs(quote.changeRate()))
                        + " (" + signed(quote.changeAmount(), market) + ")", color(quote.changeRate()))
        ));
        if (quote.high() >= 0 && quote.low() >= 0) {
            player.sendMessage(Component.text().append(
                    Component.text("금일 고가/저가: ", Palette.GRAY),
                    Component.text(market.formatMoney(CoinService.formatPrice(quote.high())), Palette.RED),
                    Component.text(" / ", Palette.GRAY),
                    Component.text(market.formatMoney(CoinService.formatPrice(quote.low())), Palette.BLUE)
            ));
        }
        if (quote.tradingValue() >= 0) {
            player.sendMessage(Component.text().append(
                    Component.text("거래대금: ", Palette.GRAY),
                    Component.text(CoinService.formatVolume(quote.tradingValue()) + "원", Palette.YELLOW)
            ));
        } else if (quote.volume() >= 0) {
            player.sendMessage(Component.text().append(
                    Component.text("거래량: ", Palette.GRAY),
                    Component.text(String.format("%,.0f주", quote.volume()), Palette.YELLOW)
            ));
        }
    }

    private void logThrottled(String message) {
        long now = System.currentTimeMillis();
        if (now - lastErrorLog < 60_000) return;
        lastErrorLog = now;
        BackasSurvivalPackExtended.getInstance().getLogger().warning(message);
    }

    private static double parse(String raw) {
        if (raw == null || raw.isBlank()) return -1;
        try {
            return Double.parseDouble(raw.replace(",", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String signed(double amount, StockMarket market) {
        String sign = amount > 0 ? "+" : amount < 0 ? "-" : "";
        return sign + market.formatMoney(CoinService.formatPrice(Math.abs(amount)));
    }

    public static String arrow(double rate) {
        if (rate > 0) return "▲";
        if (rate < 0) return "▼";
        return "─";
    }

    public static TextColor color(double rate) {
        if (rate > 0) return Palette.RED;
        if (rate < 0) return Palette.BLUE;
        return Palette.WHITE;
    }

    // ---- 응답 모델 ----

    private static class NaverPollingResponse {
        List<NaverStockData> datas;
    }

    private static class NaverStockData {
        String itemCode;
        String closePriceRaw;
        String compareToPreviousClosePriceRaw;
        String fluctuationsRatioRaw;
        String highPriceRaw;
        String lowPriceRaw;
        String accumulatedTradingValueRaw;
        String accumulatedTradingVolumeRaw;
        String marketStatus;
        CompareInfo compareToPreviousPrice;
    }

    private static class CompareInfo {
        String name;
    }

    private static class SparkEntry {
        double chartPreviousClose;
        double[] close;
    }

    private static class YahooChartResponse {
        YahooChart chart;
    }

    private static class YahooChart {
        List<YahooResult> result;
    }

    private static class YahooResult {
        YahooMeta meta;
    }

    private static class YahooMeta {
        double regularMarketPrice;
        double chartPreviousClose;
        Double regularMarketDayHigh;
        Double regularMarketDayLow;
        Double regularMarketVolume;
    }
}
