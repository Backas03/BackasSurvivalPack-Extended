package kr.kro.backas.backassurvivalpackextended.easyshop;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataPerks;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
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
        if (ItemFactory.getHead().isSimilar(item)) {
            item.setAmount(item.getAmount() - 1);
            giveSkullItem(event.getPlayer(), target);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (ItemFactory.getHead().isSimilar(item)) {
            event.setCancelled(true);
            if (!event.getPlayer().isSneaking()) {
                return;
            }
            item.setAmount(item.getAmount() - 1);
            giveSkullItem(event.getPlayer(), event.getPlayer());
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (ItemFactory.getHead().isSimilar(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTPClick(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (ItemFactory.getTPCoolTimeClear().isSimilar(item)) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            BackasSurvivalPackExtended.getTeleportManager()
                    .clearCoolTime(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Component.text("텔레포트 쿨타임이 초기화되었습니다.", NamedTextColor.YELLOW));
        }
    }

    @EventHandler
    public void onLicenseClick(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (ItemFactory.getAutoReplantLicense().isSimilar(item)) {
            event.setCancelled(true);
            useLicense(event.getPlayer(), item, UserDataPerks.AUTO_REPLANT, "자동심기");
        } else if (ItemFactory.getAutoPickupLicense().isSimilar(item)) {
            event.setCancelled(true);
            useLicense(event.getPlayer(), item, UserDataPerks.AUTO_PICKUP, "자동줍기");
        }
    }

    private void useLicense(Player player, ItemStack item, String perk, String name) {
        if (UserDataPerks.has(player, perk)) {
            player.sendMessage(Component.text("이미 " + name + " 권한을 보유하고 있습니다.", Palette.RED));
            return;
        }
        item.setAmount(item.getAmount() - 1);
        UserDataPerks.grant(player, perk);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        player.sendMessage(Component.text().append(
                Component.text("✨ ", Palette.YELLOW),
                Component.text(name, Palette.GREEN),
                Component.text(" 권한이 계정에 영구 적용되었습니다!", Palette.WHITE)
        ));
    }

    private void giveSkullItem(Player player, Player target) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(itemMeta -> {
            ((SkullMeta) itemMeta).setOwningPlayer(target);
        });
        player.getInventory().addItem(skull);
    }
}
