package kr.kro.backas.backassurvivalpackextended.ranking.data;

import kr.kro.backas.backassurvivalpackextended.ranking.RankData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class IntegerRankData extends RankData<IntegerRankData> {

    private int amount;

    public IntegerRankData(OfflinePlayer player, int amount) {
        super(player);
        this.amount = amount;
    }

    public IntegerRankData(UUID uniqueId, String playerName, int amount) {
        super(uniqueId, playerName);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public int compareTo(@NotNull IntegerRankData o) {
        return Integer.compare(o.amount, amount);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of(
                "uuid", getUniqueId(),
                "name", getPlayerName(),
                "amount", amount
        );
    }

    public static IntegerRankData deserialize(Map<String, Object> data) {
        return new IntegerRankData((UUID) data.get("uuid"),
                (String) data.get("name"),
                (int) data.get("amount"));
    }
}
