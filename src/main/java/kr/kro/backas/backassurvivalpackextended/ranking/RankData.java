package kr.kro.backas.backassurvivalpackextended.ranking;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class RankData<T> implements ConfigurationSerializable, Comparable<T> {
    private final UUID uniqueId;
    private final String playerName;

    public RankData(Player player) {
        this(player.getUniqueId(), player.getName());
    }

    public RankData(UUID uniqueId, String playerName) {
        this.uniqueId = uniqueId;
        this.playerName = playerName;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getPlayerName() {
        return playerName;
    }
}
