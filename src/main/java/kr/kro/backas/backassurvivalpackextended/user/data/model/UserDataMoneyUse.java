package kr.kro.backas.backassurvivalpackextended.user.data.model;

import kr.kro.backas.backassurvivalpackextended.user.data.UserData;
import kr.kro.backas.backassurvivalpackextended.user.data.UserDataLoader;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs("UserDataMoneyUse")
public class UserDataMoneyUse implements UserData {
    public static final String KEY = "money-use";

    public static UserDataLoader<UserDataMoneyUse> loader() {
        return yaml -> yaml.getSerializable(KEY, UserDataMoneyUse.class,
                new UserDataMoneyUse(0));
    }

    private int amount;

    public UserDataMoneyUse(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public UserDataMoneyUse setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public UserDataMoneyUse add(int amount) {
        this.amount += amount;
        return this;
    }

    public UserDataMoneyUse subtract(int amount) {
        this.amount -= amount;
        return this;
    }

    @Override
    public void setToYaml(YamlConfiguration yaml) {
        yaml.set(KEY, this);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("amount", amount);
    }

    public static UserDataMoneyUse deserialize(Map<String, Object> map) {
        return new UserDataMoneyUse((int) map.get("amount"));
    }
}
