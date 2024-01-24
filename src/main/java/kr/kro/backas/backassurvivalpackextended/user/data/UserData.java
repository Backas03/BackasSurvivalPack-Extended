package kr.kro.backas.backassurvivalpackextended.user.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface UserData extends ConfigurationSerializable {
    void setToYaml(YamlConfiguration yaml);
    String getKey();
}
