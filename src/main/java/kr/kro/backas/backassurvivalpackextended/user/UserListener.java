package kr.kro.backas.backassurvivalpackextended.user;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UserManager userManager = BackasSurvivalPackExtended.getUserManager();
        if (userManager.isLoaded(player.getUniqueId())) {
            return;
        }
        userManager.initUser(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UserManager userManager = BackasSurvivalPackExtended.getUserManager();
        if (!userManager.isLoaded(player.getUniqueId())) {
            return;
        }
        userManager.getUser(player)
                .getDataContainer()
                .saveAll();
        userManager.unloadUser(player.getUniqueId());
    }
}
