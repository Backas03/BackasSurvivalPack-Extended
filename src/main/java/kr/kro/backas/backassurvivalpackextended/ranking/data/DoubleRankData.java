package kr.kro.backas.backassurvivalpackextended.ranking.data;

import kr.kro.backas.backassurvivalpackextended.ranking.RankData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class DoubleRankData extends RankData<DoubleRankData> {

    private double amount;

    public DoubleRankData(Player player, double amount) {
        super(player);
        this.amount = amount;
    }

    public DoubleRankData(UUID uuid, String name, double amount) {
        super(uuid, name);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("uuid", getUniqueId(),
                "amount", amount);
    }

    @Override
    public int compareTo(@NotNull DoubleRankData o) {
        int compared = Double.compare(o.amount, amount);
        if (compared != 0) return compared;
        // 동점자가 TreeSet에서 중복으로 취급되어 누락되지 않도록 UUID로 구분
        return getUniqueId().compareTo(o.getUniqueId());
    }

    public static DoubleRankData deserialize(Map<String, Object> data) {
        return new DoubleRankData((UUID) data.get("uuid"),
                (String) data.get("name"),
                (double) data.get("amount"));
    }
}
