package kr.kro.backas.backassurvivalpackextended.user.data.model;

import kr.kro.backas.backassurvivalpackextended.user.data.UserData;
import kr.kro.backas.backassurvivalpackextended.user.data.UserDataLoader;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs("UserDataMoney")
public class UserDataMoney implements UserData {
    public static final String KEY = "money";

    public static UserDataLoader<UserDataMoney> loader() {
        return yaml -> yaml.getSerializable(KEY, UserDataMoney.class,
                new UserDataMoney(0));
    }

    private int amount;

    public UserDataMoney(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public UserDataMoney setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public UserDataMoney add(int amount) {
        this.amount += amount;
        return this;
    }

    public UserDataMoney subtract(int amount) {
        this.amount -= amount;
        return this;
    }

    @Override
    public void setToYaml(YamlConfiguration yaml) {
        yaml.set(KEY, this);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("amount", amount);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public static UserDataMoney deserialize(Map<String, Object> data) {
        return new UserDataMoney((int) data.get("amount"));
    }
}
