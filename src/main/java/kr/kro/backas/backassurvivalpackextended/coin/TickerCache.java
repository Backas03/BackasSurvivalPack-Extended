package kr.kro.backas.backassurvivalpackextended.coin;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KRW 전 마켓 시세 캐시. 요청 1회로 전 코인 시세를 받아 갱신한다.
 * 비동기 스레드가 쓰고, 명령어/GUI(메인 스레드)는 읽기만 한다.
 */
public final class TickerCache {

    private static final Map<String, UpbitTicker> TICKERS = new ConcurrentHashMap<>();

    private TickerCache() {
    }

    /** 비동기 스레드에서 호출 */
    public static void refresh() throws IOException {
        UpbitTicker[] tickers = UpbitClient.get("/v1/ticker/all?quote_currencies=KRW", UpbitTicker[].class);
        for (UpbitTicker ticker : tickers) {
            if (ticker.getMarket() != null) {
                TICKERS.put(ticker.getMarket(), ticker);
            }
        }
    }

    @Nullable
    public static UpbitTicker get(String marketCode) {
        return TICKERS.get(marketCode);
    }

    public static boolean isLoaded() {
        return !TICKERS.isEmpty();
    }
}
