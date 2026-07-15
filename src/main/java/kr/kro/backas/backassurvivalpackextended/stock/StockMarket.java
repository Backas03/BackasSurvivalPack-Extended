package kr.kro.backas.backassurvivalpackextended.stock;

public enum StockMarket {
    KOSPI("코스피", true),
    KOSDAQ("코스닥", true),
    NASDAQ("나스닥", false);

    private final String displayName;
    private final boolean domestic;

    StockMarket(String displayName, boolean domestic) {
        this.displayName = displayName;
        this.domestic = domestic;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDomestic() {
        return domestic;
    }

    /** 279500 -> "279,500원" / 314.86 -> "$314.86" */
    public String formatMoney(String formattedNumber) {
        return domestic ? formattedNumber + "원" : "$" + formattedNumber;
    }
}
