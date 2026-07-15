package kr.kro.backas.backassurvivalpackextended.coin;

/** GET /v1/market/all 응답 모델 (필드명은 업비트 JSON 그대로) */
public class UpbitMarket {

    private String market;
    private String korean_name;
    private String english_name;

    public String getMarket() {
        return market;
    }

    public String getKoreanName() {
        return korean_name;
    }

    public String getEnglishName() {
        return english_name;
    }

    /** KRW-BTC -> BTC */
    public String getSymbol() {
        int index = market.indexOf('-');
        return index < 0 ? market : market.substring(index + 1);
    }
}
