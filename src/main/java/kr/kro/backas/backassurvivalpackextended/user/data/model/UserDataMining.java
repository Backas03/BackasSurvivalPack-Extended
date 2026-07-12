package kr.kro.backas.backassurvivalpackextended.user.data.model;

import kr.kro.backas.backassurvivalpackextended.user.data.UserData;
import kr.kro.backas.backassurvivalpackextended.user.data.UserDataLoader;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs("UserDataMining")
public class UserDataMining implements UserData {
    public static final String KEY = "mining";

    public static UserDataLoader<UserDataMining> loader() {
        return yaml -> yaml.getSerializable(KEY, UserDataMining.class,
                new UserDataMining(0));
    }

    // 누적 광질 경험치 (레벨은 여기서 계산되므로 따로 저장하지 않는다)
    private int xp;

    public UserDataMining(int xp) {
        this.xp = xp;
    }

    public int getXp() {
        return xp;
    }

    public UserDataMining addXp(int amount) {
        this.xp += amount;
        return this;
    }

    @Override
    public void setToYaml(YamlConfiguration yaml) {
        yaml.set(KEY, this);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("xp", xp);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public static UserDataMining deserialize(Map<String, Object> data) {
        return new UserDataMining((int) data.getOrDefault("xp", 0));
    }
}
