package kr.kro.backas.backassurvivalpackextended.stock;

/**
 * 정규화된 시세. 제공되지 않는 값은 음수(-1)로 표시한다.
 *
 * @param changeRate   전일 대비 등락률 (부호 포함 %, 예: +6.27 / -1.5)
 * @param changeAmount 전일 대비 등락 금액 (부호 포함)
 * @param tradingValue 거래대금 (국내만, 원)
 * @param volume       거래량 (주)
 * @param marketOpen   장중 여부 (null = 알 수 없음)
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
        long fetchedAt
) {
    public boolean isFresh(long ttlMillis) {
        return System.currentTimeMillis() - fetchedAt < ttlMillis;
    }
}
