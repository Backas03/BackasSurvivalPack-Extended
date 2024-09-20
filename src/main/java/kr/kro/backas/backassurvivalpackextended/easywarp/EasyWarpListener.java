package kr.kro.backas.backassurvivalpackextended.easywarp;

import kr.kro.backas.backassurvivalpackextended.MoneyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EasyWarpListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(EasyWarp.INVENTORY_TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        int cost = EasyWarp.COST;
        if (!event.getClick().isLeftClick()) {
            return;
        }
        player.closeInventory();
        if (MoneyManager.getMoney(player) < cost) {
            player.sendMessage("돈이 부족합니다.");
            return;
        }
        try {
            EasyWarp.getNode(event.getRawSlot()).teleport(player).thenAccept((result) -> {
                if (result) {
                    MoneyManager.removeMoney(player, cost, true);
                    player.sendMessage(Component.text("워프 성공..!", NamedTextColor.GREEN));
                    return;
                }
                player.sendMessage(Component.text("워프에 실패했습니다.", NamedTextColor.RED));
            });
        } catch (Exception e) {
            player.sendMessage(Component.text("워프 위치를 찾을 수 없습니다.", NamedTextColor.RED));
        }
    }
}
