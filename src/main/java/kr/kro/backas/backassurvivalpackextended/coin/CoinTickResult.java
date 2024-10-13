package kr.kro.backas.backassurvivalpackextended.coin;


import java.math.BigDecimal;

public class CoinTickResult {
    public String market;
    public String trade_date;
    public String trade_time;
    public String trade_date_kst;
    public String trade_time_kst;
    public long trade_timestamp;
    public double opening_price;
    public double high_price;
    public double low_price;
    public double trade_price;
    public double prev_closing_price;
    public String change;
    public double change_price;
    public double change_rate;
    public double signed_change_price;
    public double signed_change_rate;
    public double trade_volume;
    public double acc_trade_price;
    public double acc_trade_price_24h;
    public double acc_trade_volume;
    public double acc_trade_volume_24h;
    public double highest_52_week_price;
    public String highest_52_week_date;
    public double lowest_52_week_price;
    public String lowest_52_week_date;
    public long timestamp;

    @Override
    public String toString() {
        return "time: " + trade_time_kst + ", cost: " + new BigDecimal(trade_price);
    }
}