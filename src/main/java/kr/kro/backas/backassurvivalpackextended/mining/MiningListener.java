package kr.kro.backas.backassurvivalpackextended.mining;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMining;
import net.kyori.adventure.text.Component;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.concurrent.ThreadLocalRandom;

public class MiningListener implements Listener {

    // 플레이어가 직접 설치한 광석 표시 (실크터치로 캔 광석 재설치->채굴 악용 방지)
    private static final String PLACED_METADATA = "bspe-mining-placed";

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (MiningManager.isOre(event.getBlock().getType())) {
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
        if (!MiningManager.isOre(type)) return;

        if (block.hasMetadata(PLACED_METADATA)) {
            block.removeMetadata(PLACED_METADATA, BackasSurvivalPackExtended.getInstance());
            return;
        }
        // 적정 도구가 아니면 (드랍이 없으므로) 경험치도 없음
        if (!block.isPreferredTool(player.getInventory().getItemInMainHand())) {
            return;
        }

        int xp = MiningManager.getOreXp(type);
        MiningManager.addXp(player, xp);

        UserDataMining data = MiningManager.getUserData(player);
        int level = MiningManager.getLevel(data.getXp());

        int extraAmount = 0;
        if (level > 0 && ThreadLocalRandom.current().nextDouble() < MiningManager.getExtraDropChance(level)) {
            extraAmount = MiningManager.rollExtraDropAmount(level);
            Material extraDrop = MiningManager.getExtraDrop(type);
            if (extraDrop != null) {
                block.getWorld().dropItemNaturally(
                        block.getLocation().add(0.5, 0.5, 0.5),
                        new ItemStack(extraDrop, extraAmount)
                );
            }
        }

        String progress = level >= MiningManager.MAX_LEVEL
                ? "MAX"
                : MiningManager.getXpIntoLevel(data.getXp()) + "/" + MiningManager.xpToNext(level);
        player.sendActionBar(Component.text().append(
                Component.text("⛏ +" + xp + " 경험치 ", Palette.AQUA),
                extraAmount > 0
                        ? Component.text("💎 추가 채굴 +" + extraAmount + "! ", Palette.GOLD)
                        : Component.empty(),
                Component.text("(광질 Lv." + level + " · " + progress + ")", Palette.GRAY)
        ).build());
    }
}
