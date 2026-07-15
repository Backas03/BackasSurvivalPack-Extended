package kr.kro.backas.backassurvivalpackextended.stock;

/** 종목 하나 (code: 국내 6자리 코드 또는 나스닥 심볼) */
public record Stock(StockMarket market, String code, String name) {

    public String key() {
        return market.name() + ":" + code;
    }
}
