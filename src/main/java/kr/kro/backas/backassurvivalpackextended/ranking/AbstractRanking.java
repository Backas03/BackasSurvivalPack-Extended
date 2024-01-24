package kr.kro.backas.backassurvivalpackextended.ranking;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.UUID;

public abstract class AbstractRanking<T extends RankData<?>> {
    public abstract @NotNull String getName();
    public abstract Listener getUpdateListener();
    public abstract void send(Player player, int displayAmount, int page);
    protected abstract void load();

    protected final TreeSet<T> ranks;

    protected AbstractRanking() {
        this.ranks = new TreeSet<>();
        load(); // call after rankings is initialized
    }

    public @NotNull TreeSet<T> getRanks() {
        return ranks;
    }

    public T getRankData(int rank) {
        return new ArrayList<>(ranks).get(rank);
    }

    public void removeData(T data) {
        ranks.remove(data);
    }

    public int getRank(UUID uuid) {
        int rank = 1;
        for (T data : ranks) {
            if (data.getUniqueId().equals(uuid)) {
                return rank;
            }
            rank++;
        }
        return -1;
    }

    public int getRank(Player player) {
        return getRank(player.getUniqueId());
    }

    public T getRankData(String playerName) {
        for (T data : ranks) {
            if (data.getPlayerName().equals(playerName)) {
                return data;
            }
        }
        return null;
    }

    public T getRankData(UUID uuid) {
        for (T data : ranks) {
            if (data .getUniqueId().equals(uuid)) {
                return data;
            }
        }
        return null;
    }

    public T getRankData(Player player) {
        return getRankData(player.getUniqueId());
    }
}
