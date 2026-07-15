package kr.kro.backas.backassurvivalpackextended.stock;

import org.jetbrains.annotations.Nullable;

/**
 * 정규화된 시세. 제공되지 않는 값은 음수(-1)로 표시한다.
 *
 * @param changeRate      전일 대비 등락률 (부호 포함 %, 예: +6.27 / -1.5)
 * @param changeAmount    전일 대비 등락 금액 (부호 포함)
 * @param tradingValue    거래대금 (국내만, 원)
 * @param volume          거래량 (주)
 * @param marketOpen      정규장 장중 여부 (null = 알 수 없음)
 * @param session         현재 세션 라벨: "정규장"/"프리장"/"애프터장"/"장마감" (null = 정보 없음)
 * @param overPrice       프리장/애프터장 현재가 (-1 = 없음)
 * @param overChangeRate  장외 등락률 (전일 종가 대비, 부호 포함 %)
 * @param overChangeAmount 장외 등락 금액 (부호 포함)
 */
public record StockQuote(
        double price,
        double changeRate,
        double changeAmount,
        double high,
        double low,
        double tradingValue,
        double volume,
        Boolean marketOpen,
        @Nullable String session,
        double overPrice,
        double overChangeRate,
        double overChangeAmount,
        long fetchedAt
) {
    public boolean isFresh(long ttlMillis) {
        return System.currentTimeMillis() - fetchedAt < ttlMillis;
    }

    /** 장외(프리장/애프터장) 시세를 표시해야 하는 상태인가 */
    public boolean hasOverMarketPrice() {
        return overPrice > 0 && ("프리장".equals(session) || "애프터장".equals(session));
    }
}
