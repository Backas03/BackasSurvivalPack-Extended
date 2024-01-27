package kr.kro.backas.backassurvivalpackextended.ranking;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.ranking.model.DeathRanking;
import kr.kro.backas.backassurvivalpackextended.ranking.model.MoneyRanking;
import kr.kro.backas.backassurvivalpackextended.ranking.model.PlayTimeRanking;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RankingManager {
    private final Map<Class<?>, AbstractRanking<?>> registeredRankings;

    public RankingManager() {
        registeredRankings = new HashMap<>();

        registerRanking(new MoneyRanking());
        registerRanking(new DeathRanking());
        registerRanking(new PlayTimeRanking());
    }

    public void registerRanking(AbstractRanking<?> ranking) {
        if (ranking.getUpdateListener() != null)
            Bukkit.getPluginManager().registerEvents(ranking.getUpdateListener(), BackasSurvivalPackExtended.getInstance());
        registeredRankings.put(ranking.getClass(), ranking);
    }

    @Nullable
    public <T extends AbstractRanking<?>> T getRanking(Class<T> rankingClass) {
        return rankingClass.cast(registeredRankings.get(rankingClass));
    }

    @Nullable
    public AbstractRanking<?> getRanking(String rankingName) {
        return registeredRankings.values().stream()
                .filter(ranking -> ranking.getName().equalsIgnoreCase(rankingName))
                .findFirst().orElse(null);
    }

    public Collection<AbstractRanking<?>> getAllRankings() {
        return registeredRankings.values();
    }
}
