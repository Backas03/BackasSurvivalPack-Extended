package kr.kro.backas.backassurvivalpackextended.ranking;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.ranking.model.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RankingManager {
    public static final long UPDATE_INTERVAL = 20 * 60 * 5L;

    private Map<Class<?>, AbstractRanking<?>> registeredRankings;
    private LocalDateTime lastUpdate;

    public RankingManager() {
        registeredRankings = new HashMap<>();

        update(true);

        Bukkit.getScheduler().runTaskTimerAsynchronously(BackasSurvivalPackExtended.getInstance(),
                () -> update(false),
                UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    public void update(boolean registerListener) {
        registeredRankings = new HashMap<>();

        registerRanking(new MoneyRanking(), registerListener);
        registerRanking(new DeathRanking(), registerListener);
        registerRanking(new PlayTimeRanking(), registerListener);
        registerRanking(new DamageRanking(), registerListener);
        registerRanking(new HuntRanking(), registerListener);
        registerRanking(new PlayerKillRanking(), registerListener);
        registerRanking(new MoneyUseRanking(), registerListener);

        lastUpdate = LocalDateTime.now();
    }

    public void registerRanking(AbstractRanking<?> ranking, boolean registerListener) {
        registeredRankings.remove(ranking.getClass());
        if (ranking.getUpdateListener() != null && registerListener)
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

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdateToNow() {
        this.lastUpdate = LocalDateTime.now();
    }
}
