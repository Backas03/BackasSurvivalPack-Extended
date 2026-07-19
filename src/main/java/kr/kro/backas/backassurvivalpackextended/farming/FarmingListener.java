package kr.kro.backas.backassurvivalpackextended.farming;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataFarming;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataPerks;
import org.jetbrains.annotations.Nullable;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import kr.kro.backas.backassurvivalpackextended.util.PlacedBlockTracker;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FarmingListener implements Listener {

    // 수확 시 자동으로 다시 심을 씨앗 (작물 블록 -> 씨앗 아이템)
    private static final Map<Material, Material> REPLANT_SEEDS = Map.of(
            Material.WHEAT, Material.WHEAT_SEEDS,
            Material.CARROTS, Material.CARROT,
            Material.POTATOES, Material.POTATO,
            Material.BEETROOTS, Material.BEETROOT_SEEDS,
            Material.NETHER_WART, Material.NETHER_WART
    );

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Material type = event.getBlock().getType();
        // 성장 단계가 있는 작물은 age 검사로 걸러지므로, 설치 악용이 가능한 작물만 표시한다.
        if (FarmingManager.isCrop(type) && FarmingManager.SKIP_AGE_CHECK.contains(type)) {
            PlacedBlockTracker.mark(event.getBlock());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();
        if (!FarmingManager.isCrop(type)) return;

        // 크리에이티브 파괴여도 설치 표시는 지워져야 하므로 게임모드 검사보다 먼저 처리한다.
        boolean placedByPlayer = PlacedBlockTracker.unmark(block);
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (placedByPlayer) return;

        // 다 자란 작물만 인정
        if (!FarmingManager.SKIP_AGE_CHECK.contains(type)
                && block.getBlockData() instanceof Ageable ageable
                && ageable.getAge() < ageable.getMaximumAge()) {
            return;
        }

        boolean autoPickup = UserDataPerks.has(player, UserDataPerks.AUTO_PICKUP);
        boolean autoReplant = UserDataPerks.has(player, UserDataPerks.AUTO_REPLANT);

        // 자동줍기 (권한부여서 필요): 드랍을 바닥에 떨어뜨리지 않고 인벤토리로 직접 지급
        List<ItemStack> drops = null;
        if (autoPickup) {
            drops = new ArrayList<>(block.getDrops(player.getInventory().getItemInMainHand(), player));
            event.setDropItems(false);
        }

        // 자동심기 (권한부여서 필요): 씨앗 1개를 소모해 같은 자리에 다시 심는다 (드랍 우선, 없으면 인벤토리)
        Material seed = REPLANT_SEEDS.get(type);
        if (autoReplant && seed != null && consumeSeed(player, drops, seed)) {
            Bukkit.getScheduler().runTask(BackasSurvivalPackExtended.getInstance(), () -> {
                if (block.getType() == Material.AIR) {
                    block.setType(type);
                }
            });
        }
        if (drops != null) {
            giveItems(player, block, drops);
        }

        harvest(player, block, type, autoPickup);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getHarvestedBlock();
        Material type = block.getType();
        if (!FarmingManager.isCrop(type)) return;

        harvest(player, block, type, UserDataPerks.has(player, UserDataPerks.AUTO_PICKUP));
    }

    private boolean consumeSeed(Player player, @Nullable List<ItemStack> drops, Material seed) {
        if (drops != null) {
            for (ItemStack drop : drops) {
                if (drop != null && drop.getType() == seed && drop.getAmount() > 0) {
                    drop.setAmount(drop.getAmount() - 1);
                    return true;
                }
            }
        }
        // 드랍에 씨앗이 없으면 인벤토리에서 소모
        return player.getInventory().removeItem(new ItemStack(seed, 1)).isEmpty();
    }

    private static void giveItems(Player player, Block block, Collection<ItemStack> items) {
        for (ItemStack item : items) {
            if (item == null || item.getAmount() <= 0) continue;
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            for (ItemStack left : overflow.values()) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), left);
            }
        }
    }

    private void harvest(Player player, Block block, Material cropType, boolean autoPickup) {
        int xp = FarmingManager.getCropXp(cropType);
        FarmingManager.addXp(player, xp);

        UserDataFarming data = FarmingManager.getUserData(player);
        int level = FarmingManager.getLevel(data.getXp());

        int extraAmount = 0;
        Material extraDrop = FarmingManager.getExtraDrop(cropType);
        if (level > 0 && extraDrop != null
                && ThreadLocalRandom.current().nextDouble() < FarmingManager.getExtraDropChance(level)) {
            extraAmount = FarmingManager.rollExtraDropAmount(level);
            if (autoPickup) {
                giveItems(player, block, List.of(new ItemStack(extraDrop, extraAmount)));
            } else {
                block.getWorld().dropItemNaturally(
                        block.getLocation().add(0.5, 0.5, 0.5),
                        new ItemStack(extraDrop, extraAmount)
                );
            }
            player.sendMessage(Component.text().append(
                    Component.text("[농사] ", Palette.GREEN),
                    Component.text("🍀 특전 발동! ", Palette.GOLD),
                    Component.translatable(extraDrop.translationKey()).color(Palette.WHITE),
                    Component.text(" " + extraAmount + "개를 추가로 획득했어요!", Palette.WHITE)
            ));
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
