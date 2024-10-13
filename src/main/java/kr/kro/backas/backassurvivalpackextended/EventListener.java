package kr.kro.backas.backassurvivalpackextended;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (entity.getType() != EntityType.PLAYER || !entity.isSneaking()) return;
        entity.addPassenger(player);
    }

    // 팬텀 스폰 방지
    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.PHANTOM) {
            event.setCancelled(true);
        }
    }

    // 크리퍼 폭파 방지
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (event.getEntityType() == EntityType.CREEPER) {
            event.blockList().clear();
        }
    }

    // 경작지 파괴 방지
    @EventHandler
    public void noUproot(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.FARMLAND)
            event.setCancelled(true);
    }


    // 엔더맨 블럭 드는 거 방지
    @EventHandler
    public void onEndermanHoldBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.ENDERMAN) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // 마지막 죽은자리 좌표 띄우기
        Location location = event.getPlayer().getLocation();
        event.getPlayer()
                .sendMessage(Component.text("마지막 죽은 자리 좌표: " + String.format(
                        "world=%s, x=%d, y=%d, z=%d",
                        location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ()
                ), NamedTextColor.YELLOW));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // 금 검 드랍 금지
        //event.getDrops().removeIf(item -> item.getType() == Material.GOLDEN_SWORD);
    }
}
