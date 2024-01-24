package kr.kro.backas.backassurvivalpackextended.user.data;

import org.bukkit.configuration.file.YamlConfiguration;

@FunctionalInterface
public interface UserDataLoader<T> {
    T load(YamlConfiguration yaml);
}
