package kr.kro.backas.backassurvivalpackextended.user.data.model;

import kr.kro.backas.backassurvivalpackextended.user.data.UserData;
import kr.kro.backas.backassurvivalpackextended.user.data.UserDataLoader;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SerializableAs("UserDataPoint")
public class UserDataPoint implements UserData {
    public static final String KEY = "point";

    public static UserDataLoader<UserDataPoint> loader() {
        return yaml -> yaml.getSerializable(KEY, UserDataPoint.class,
                new UserDataPoint(0, new ArrayList<>(), null, 0));
    }

    private int amount;
    private final List<String> ownedTitles;
    private String equippedTitle;
    private int totalEarned;

    public UserDataPoint(int amount, List<String> ownedTitles, @Nullable String equippedTitle, int totalEarned) {
        this.amount = amount;
        this.ownedTitles = ownedTitles;
        this.equippedTitle = equippedTitle;
        this.totalEarned = totalEarned;
    }

    public int getAmount() {
        return amount;
    }

    public UserDataPoint setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public UserDataPoint add(int amount) {
        this.amount += amount;
        return this;
    }

    public UserDataPoint subtract(int amount) {
        this.amount -= amount;
        return this;
    }

    public int getTotalEarned() {
        return totalEarned;
    }

    public UserDataPoint addEarned(int amount) {
        this.totalEarned += amount;
        return this;
    }

    public boolean hasTitle(String titleId) {
        return ownedTitles.contains(titleId);
    }

    public void addTitle(String titleId) {
        if (!ownedTitles.contains(titleId)) {
            ownedTitles.add(titleId);
        }
    }

    @Nullable
    public String getEquippedTitle() {
        return equippedTitle;
    }

    public void setEquippedTitle(@Nullable String titleId) {
        this.equippedTitle = titleId;
    }

    @Override
    public void setToYaml(YamlConfiguration yaml) {
        yaml.set(KEY, this);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", amount);
        data.put("ownedTitles", ownedTitles);
        data.put("equippedTitle", equippedTitle == null ? "" : equippedTitle);
        data.put("totalEarned", totalEarned);
        return data;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @SuppressWarnings("unchecked")
    public static UserDataPoint deserialize(Map<String, Object> data) {
        int amount = (int) data.getOrDefault("amount", 0);
        List<String> ownedTitles = data.get("ownedTitles") instanceof List<?> list
                ? new ArrayList<>((List<String>) list)
                : new ArrayList<>();
        String equipped = (String) data.getOrDefault("equippedTitle", "");
        // 기존 데이터에는 totalEarned가 없으므로 현재 보유량을 초기값으로 사용한다.
        int totalEarned = (int) data.getOrDefault("totalEarned", amount);
        return new UserDataPoint(amount, ownedTitles, equipped.isEmpty() ? null : equipped, totalEarned);
    }
}
