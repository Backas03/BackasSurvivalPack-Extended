package kr.kro.backas.backassurvivalpackextended.farming;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataFarming;
import net.kyori.adventure.text.Component;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.concurrent.ThreadLocalRandom;

public class FarmingListener implements Listener {

    // 플레이어가 직접 설치한 블록 표시 (설치->파괴 반복으로 경험치 버는 것 방지)
    private static final String PLACED_METADATA = "bspe-farming-placed";

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Material type = event.getBlock().getType();
        // 성장 단계가 있는 작물은 age 검사로 걸러지므로, 설치 악용이 가능한 작물만 표시한다.
        if (FarmingManager.isCrop(type) && FarmingManager.SKIP_AGE_CHECK.contains(type)) {
            event.getBlock().setMetadata(PLACED_METADATA,
                    new FixedMetadataValue(BackasSurvivalPackExtended.getInstance(), true));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        Material type = block.getType();
        if (!FarmingManager.isCrop(type)) return;

        if (block.hasMetadata(PLACED_METADATA)) {
            block.removeMetadata(PLACED_METADATA, BackasSurvivalPackExtended.getInstance());
            return;
        }
        // 다 자란 작물만 인정
        if (!FarmingManager.SKIP_AGE_CHECK.contains(type)
                && block.getBlockData() instanceof Ageable ageable
                && ageable.getAge() < ageable.getMaximumAge()) {
            return;
        }

        harvest(player, block, type);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getHarvestedBlock();
        Material type = block.getType();
        if (!FarmingManager.isCrop(type)) return;

        harvest(player, block, type);
    }

    private void harvest(Player player, Block block, Material cropType) {
        int xp = FarmingManager.getCropXp(cropType);
        FarmingManager.addXp(player, xp);

        UserDataFarming data = FarmingManager.getUserData(player);
        int level = FarmingManager.getLevel(data.getXp());

        int extraAmount = 0;
        if (level > 0 && ThreadLocalRandom.current().nextDouble() < FarmingManager.getExtraDropChance(level)) {
            extraAmount = FarmingManager.rollExtraDropAmount(level);
            Material extraDrop = FarmingManager.getExtraDrop(cropType);
            if (extraDrop != null) {
                block.getWorld().dropItemNaturally(
                        block.getLocation().add(0.5, 0.5, 0.5),
                        new ItemStack(extraDrop, extraAmount)
                );
            }
        }

        String progress = level >= FarmingManager.MAX_LEVEL
                ? "MAX"
                : FarmingManager.getXpIntoLevel(data.getXp()) + "/" + FarmingManager.xpToNext(level);
        player.sendActionBar(Component.text().append(
                Component.text("🌾 +" + xp + " 경험치 ", Palette.GREEN),
                extraAmount > 0
                        ? Component.text("🍀 추가 수확 +" + extraAmount + "! ", Palette.GOLD)
                        : Component.empty(),
                Component.text("(농사 Lv." + level + " · " + progress + ")", Palette.GRAY)
        ).build());
    }
}
