package kr.kro.backas.backassurvivalpackextended.point.title;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import kr.kro.backas.backassurvivalpackextended.api.UserDataPreLoadDoneEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TitleListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Title title = TitleManager.getEquipped(event.getPlayer());
        if (title == null) return;
        event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, message) ->
                Component.text().append(
                        title.display(),
                        Component.space(),
                        Component.text("<", NamedTextColor.WHITE),
                        sourceDisplayName,
                        Component.text("> ", NamedTextColor.WHITE),
                        message
                ).build()
        ));
    }

    @EventHandler
    public void onDataLoaded(UserDataPreLoadDoneEvent event) {
        Player player = event.getUser().getPlayer();
        if (player == null) return;
        TitleManager.applyTabName(player);
    }
}
