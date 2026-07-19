package kr.kro.backas.backassurvivalpackextended.user.data.model;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.user.data.UserData;
import kr.kro.backas.backassurvivalpackextended.user.data.UserDataLoader;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 권한부여서로 해금하는 영구 특전 (자동심기/자동줍기 등) */
@SerializableAs("UserDataPerks")
public class UserDataPerks implements UserData {
    public static final String KEY = "perks";

    public static final String AUTO_REPLANT = "AUTO_REPLANT";
    public static final String AUTO_PICKUP = "AUTO_PICKUP";

    public static UserDataLoader<UserDataPerks> loader() {
        return yaml -> yaml.getSerializable(KEY, UserDataPerks.class,
                new UserDataPerks(new ArrayList<>()));
    }

    private final List<String> perks;

    public UserDataPerks(List<String> perks) {
        this.perks = perks;
    }

    public boolean hasPerk(String perk) {
        return perks.contains(perk);
    }

    public void addPerk(String perk) {
        if (!perks.contains(perk)) {
            perks.add(perk);
        }
    }

    public static boolean has(Player player, String perk) {
        return BackasSurvivalPackExtended.getUserManager()
                .getUser(player)
                .getDataContainer()
                .getOrLoad(UserDataPerks.class)
                .hasPerk(perk);
    }

    public static void grant(Player player, String perk) {
        BackasSurvivalPackExtended.getUserManager()
                .getUser(player)
                .getDataContainer()
                .getOrLoad(UserDataPerks.class)
                .addPerk(perk);
    }

    @Override
    public void setToYaml(YamlConfiguration yaml) {
        yaml.set(KEY, this);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("perks", perks);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @SuppressWarnings("unchecked")
    public static UserDataPerks deserialize(Map<String, Object> data) {
        List<String> perks = data.get("perks") instanceof List<?> list
                ? new ArrayList<>((List<String>) list)
                : new ArrayList<>();
        return new UserDataPerks(perks);
    }
}
