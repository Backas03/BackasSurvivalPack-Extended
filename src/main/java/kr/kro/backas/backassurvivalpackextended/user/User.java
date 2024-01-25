package kr.kro.backas.backassurvivalpackextended.user;

import kr.kro.backas.backassurvivalpackextended.user.data.UserDataContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class User {
    private final UUID uniqueId;
    private final String name;

    private final UserDataContainer dataContainer;

    public User(Player player) {
        this(player.getUniqueId(), player.getName());
    }

    public User(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.dataContainer = new UserDataContainer(this);
    }

    public UserDataContainer getDataContainer() {
        return dataContainer;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }
}
