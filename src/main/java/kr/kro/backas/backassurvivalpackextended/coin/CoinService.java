package kr.kro.backas.backassurvivalpackextended.coin;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 코인 시세 시스템 진입점.
 * - 5초마다 KRW 전 마켓 시세 갱신 + LIVE 구독자 액션바 전송
 * - 6시간마다 마켓 목록(한글명 포함) 갱신
 */
public class CoinService {

    private static final long TICKER_INTERVAL = 20L * 5;
    private static final long MARKET_REFRESH_INTERVAL = 20L * 60 * 60 * 6;

    // 플레이어별 LIVE 액션바 구독 코인 (마켓코드)
    private final Map<UUID, String> liveSubscriptions = new ConcurrentHashMap<>();
    private long lastErrorLog = 0;

    public void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
            try {
                MarketRegistry.refresh();
            } catch (Exception e) {
                logThrottled("업비트 마켓 목록 갱신 실패: " + e.getMessage());
            }
        }, 0L, MARKET_REFRESH_INTERVAL);

        Bukkit.getScheduler().runTaskTimerAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
            try {
                TickerCache.refresh();
                sendLiveActionBars();
            } catch (Exception e) {
                logThrottled("업비트 시세 갱신 실패: " + e.getMessage());
            }
        }, 20L, TICKER_INTERVAL);
    }

    private void sendLiveActionBars() {
        for (Map.Entry<UUID, String> entry : liveSubscriptions.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;
            UpbitTicker ticker = TickerCache.get(entry.getValue());
            UpbitMarket market = MarketRegistry.byCode(entry.getValue());
            if (ticker == null || market == null) continue;
            player.sendActionBar(Component.text().append(
                    Component.text("[LIVE] ", TextColor.color(0x86E57F)),
                    Component.text(market.getKoreanName() + " ", Palette.GOLD),
                    Component.text(formatPrice(ticker.getTradePrice()) + "원 ", Palette.WHITE),
                    Component.text(changeArrow(ticker.getSignedChangeRate())
                            + formatRate(ticker.getSignedChangeRate()), changeColor(ticker.getSignedChangeRate()))
            ).build());
        }
    }

    public void toggleLive(Player player, UpbitMarket market) {
        String current = liveSubscriptions.get(player.getUniqueId());
        if (market.getMarket().equals(current)) {
            liveSubscriptions.remove(player.getUniqueId());
            player.sendMessage(Component.text().append(
                    Component.text("[코인] ", Palette.GOLD),
                    Component.text(market.getKoreanName(), Palette.WHITE),
                    Component.text(" LIVE 알림을 껐습니다.", Palette.GRAY)
            ));
        } else {
            liveSubscriptions.put(player.getUniqueId(), market.getMarket());
            player.sendMessage(Component.text().append(
                    Component.text("[코인] ", Palette.GOLD),
                    Component.text(market.getKoreanName() + " (" + market.getSymbol() + ")", Palette.WHITE),
                    Component.text(" LIVE 알림을 켰습니다. 액션바에 실시간 시세가 표시됩니다.", Palette.GRAY)
            ));
        }
    }

    /** 채팅으로 코인 상세 시세를 보여준다. */
    public static void sendDetail(Player player, UpbitMarket market) {
        UpbitTicker ticker = TickerCache.get(market.getMarket());
        if (ticker == null) {
            player.sendMessage(Component.text("아직 시세 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.", Palette.RED));
            return;
        }
        TextColor color = changeColor(ticker.getSignedChangeRate());
        player.sendMessage(Component.text().append(
                Component.text("[코인] ", Palette.GOLD),
                Component.text(market.getKoreanName(), Palette.WHITE),
                Component.text(" (" + market.getSymbol() + ")", Palette.GRAY)
        ));
        player.sendMessage(Component.text().append(
                Component.text("현재가: ", Palette.GRAY),
                Component.text(formatPrice(ticker.getTradePrice()) + "원 ", Palette.WHITE),
                Component.text(changeArrow(ticker.getSignedChangeRate())
                        + formatRate(ticker.getSignedChangeRate())
                        + " (" + signedPrice(ticker.getSignedChangePrice()) + "원)", color)
        ));
        player.sendMessage(Component.text().append(
                Component.text("24h 고가/저가: ", Palette.GRAY),
                Component.text(formatPrice(ticker.getHighPrice()), Palette.RED),
                Component.text(" / ", Palette.GRAY),
                Component.text(formatPrice(ticker.getLowPrice()), Palette.BLUE)
        ));
        player.sendMessage(Component.text().append(
                Component.text("24h 거래대금: ", Palette.GRAY),
                Component.text(formatVolume(ticker.getAccTradePrice24h()) + "원", Palette.YELLOW)
        ));
    }

    private void logThrottled(String message) {
        long now = System.currentTimeMillis();
        if (now - lastErrorLog < 60_000) return;
        lastErrorLog = now;
        BackasSurvivalPackExtended.getInstance().getLogger().warning(message);
    }

    // ---- 표시 형식 헬퍼 ----

    public static String formatPrice(double price) {
        if (price >= 1000) return String.format("%,.0f", price);
        if (price >= 1) return String.format("%,.2f", price);
        return String.format("%,.4f", price);
    }

    /** 0.0123 -> "1.23%" */
    public static String formatRate(double rate) {
        return String.format("%.2f%%", Math.abs(rate * 100));
    }

    public static String signedPrice(double price) {
        return (price > 0 ? "+" : price < 0 ? "-" : "") + formatPrice(Math.abs(price));
    }

    public static String changeArrow(double rate) {
        if (rate > 0) return "▲";
        if (rate < 0) return "▼";
        return "─";
    }

    /** 국내 관례: 상승 빨강, 하락 파랑 */
    public static TextColor changeColor(double rate) {
        if (rate > 0) return Palette.RED;
        if (rate < 0) return Palette.BLUE;
        return Palette.WHITE;
    }

    public static String formatVolume(double volume) {
        if (volume >= 1e12) return String.format("%,.1f조", volume / 1e12);
        if (volume >= 1e8) return String.format("%,.0f억", volume / 1e8);
        if (volume >= 1e4) return String.format("%,.0f만", volume / 1e4);
        return String.format("%,.0f", volume);
    }
}
