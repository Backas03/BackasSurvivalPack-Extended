package kr.kro.backas.backassurvivalpackextended.ranking;

import kr.kro.backas.backassurvivalpackextended.ranking.model.MoneyRanking;

public enum RankType {
    MONEY(MoneyRanking.class);

    private final Class<? extends AbstractRanking<?>> rankingType;

    RankType(Class<? extends AbstractRanking<?>> rankingType) {
        this.rankingType = rankingType;
    }

    public Class<? extends AbstractRanking<?>> getRankingType() {
        return rankingType;
    }
}
