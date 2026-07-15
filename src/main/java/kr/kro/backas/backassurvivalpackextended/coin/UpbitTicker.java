package kr.kro.backas.backassurvivalpackextended.coin;

/** GET /v1/ticker/all 응답 모델 (필드명은 업비트 JSON 그대로) */
public class UpbitTicker {

    private String market;
    private double trade_price;
    private double signed_change_rate;
    private double signed_change_price;
    private double high_price;
    private double low_price;
    private double acc_trade_price_24h;

    public String getMarket() {
        return market;
    }

    /** 현재가 */
    public double getTradePrice() {
        return trade_price;
    }

    /** 전일 종가 대비 등락률 (부호 포함, 0.01 = +1%) */
    public double getSignedChangeRate() {
        return signed_change_rate;
    }

    /** 전일 종가 대비 등락 금액 (부호 포함) */
    public double getSignedChangePrice() {
        return signed_change_price;
    }

    public double getHighPrice() {
        return high_price;
    }

    public double getLowPrice() {
        return low_price;
    }

    /** 24시간 누적 거래대금 (원) */
    public double getAccTradePrice24h() {
        return acc_trade_price_24h;
    }
}
