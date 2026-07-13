package kr.kro.backas.backassurvivalpackextended.mining;

import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMining;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import kr.kro.backas.backassurvivalpackextended.util.PlacedBlockTracker;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class MiningListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // 실크터치로 캔 광석을 재설치->채굴하는 악용 방지
        if (MiningManager.isOre(event.getBlock().getType())) {
            PlacedBlockTracker.mark(event.getBlock());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();
        if (!MiningManager.isOre(type)) return;

        // 크리에이티브 파괴여도 설치 표시는 지워져야 하므로 게임모드 검사보다 먼저 처리한다.
        boolean placedByPlayer = PlacedBlockTracker.unmark(block);
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (placedByPlayer) return;

        // 적정 도구가 아니면 (드랍이 없으므로) 경험치도 없음
        if (!block.isPreferredTool(player.getInventory().getItemInMainHand())) {
            return;
        }

        int xp = MiningManager.getOreXp(type);
        MiningManager.addXp(player, xp);

        UserDataMining data = MiningManager.getUserData(player);
        int level = MiningManager.getLevel(data.getXp());

        int extraAmount = 0;
        Material extraDrop = MiningManager.getExtraDrop(type);
        if (level > 0 && extraDrop != null
                && ThreadLocalRandom.current().nextDouble() < MiningManager.getExtraDropChance(level)) {
            extraAmount = MiningManager.rollExtraDropAmount(level);
            block.getWorld().dropItemNaturally(
                    block.getLocation().add(0.5, 0.5, 0.5),
                    new ItemStack(extraDrop, extraAmount)
            );
            player.sendMessage(Component.text().append(
                    Component.text("[광질] ", Palette.AQUA),
                    Component.text("💎 특전 발동! ", Palette.GOLD),
                    Component.translatable(extraDrop.translationKey()).color(Palette.WHITE),
                    Component.text(" " + extraAmount + "개를 추가로 획득했어요!", Palette.WHITE)
            ));
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
