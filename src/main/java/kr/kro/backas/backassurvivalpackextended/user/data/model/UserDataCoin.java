package kr.kro.backas.backassurvivalpackextended.user.data.model;

import kr.kro.backas.backassurvivalpackextended.user.data.UserData;
import kr.kro.backas.backassurvivalpackextended.user.data.UserDataLoader;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs("UserDataCoin")
public class UserDataCoin implements UserData {
    public static final String KEY = "coin";

    public static UserDataLoader<UserDataCoin> loader() {
        return yaml -> yaml.getSerializable(KEY, UserDataCoin.class,
                new UserDataCoin(0d));
    }

    private double bitcoin;

    public UserDataCoin(double bitcoin) {
        this.bitcoin = bitcoin;
    }

    public double getBitcoin() {
        return bitcoin;
    }

    public UserDataCoin setBitcoin(double bitcoin) {
        this.bitcoin = bitcoin;
        return this;
    }

    @Override
    public void setToYaml(YamlConfiguration yaml) {
        yaml.set(KEY, this);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("bitcoin", bitcoin);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public static UserDataCoin deserialize(Map<String, Object> data) {
        return new UserDataCoin(
                (double) data.get("bitcoin")
        );
    }
}
