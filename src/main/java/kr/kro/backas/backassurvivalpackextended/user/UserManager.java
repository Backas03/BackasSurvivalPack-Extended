package kr.kro.backas.backassurvivalpackextended.user;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class UserManager {
    private final Map<UUID, User> loadedUsers;

    public UserManager() {
        this.loadedUsers = new HashMap<>();
        final int saveInterval = 20 * 600;
        Bukkit.getScheduler().runTaskTimerAsynchronously(BackasSurvivalPackExtended.getInstance(),
                this::saveAll, saveInterval, saveInterval);
    }

    public void saveAll() {
        for (User user : loadedUsers.values()) {
            user.getDataContainer().saveAll();
        }
    }

    public User getUserUnsafe(UUID uniqueId) {
        return loadedUsers.get(uniqueId);
    }

    public User getUser(Player player) {
        return loadedUsers.computeIfAbsent(player.getUniqueId(), k -> new User(player));
    }

    public User newInstance(OfflinePlayer player) {
        return new User(player.getUniqueId(), player.getName());
    }

    public User initUser(Player player) {
        User user = new User(player);
        loadedUsers.put(player.getUniqueId(), user);
        return user;
    }

    public boolean isLoaded(UUID uniqueId) {
        return loadedUsers.containsKey(uniqueId);
    }

    public void unloadUser(UUID uniqueId) {
        loadedUsers.remove(uniqueId);
    }
}
