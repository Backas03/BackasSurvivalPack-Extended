package kr.kro.backas.backassurvivalpackextended.easyshop;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemListener implements Listener {
    @EventHandler
    public void onEntityClick(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }
        if (event.getPlayer().isSneaking()) {
            return;
        }
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (Item.getHead().isSimilar(item)) {
            item.setAmount(item.getAmount() - 1);
            giveSkullItem(event.getPlayer(), target);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (Item.getHead().isSimilar(item)) {
            event.setCancelled(true);
            if (!event.getPlayer().isSneaking()) {
                return;
            }
            item.setAmount(item.getAmount() - 1);
            giveSkullItem(event.getPlayer(), event.getPlayer());
        }
    }

    @EventHandler
    public void onTPClick(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (Item.getTPCoolTimeClear().isSimilar(item)) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            BackasSurvivalPackExtended.getTeleportManager()
                    .clearCoolTime(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Component.text("텔레포트 쿨타임이 초기화되었습니다.", NamedTextColor.YELLOW));
        }
    }

    private void giveSkullItem(Player player, Player target) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(itemMeta -> {
            ((SkullMeta) itemMeta).setOwningPlayer(target);
        });
        player.getInventory().addItem(skull);
    }
}
