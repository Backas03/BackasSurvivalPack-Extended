package kr.kro.backas.backassurvivalpackextended.farming;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataFarming;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class FarmingManager {

    public static final int MAX_LEVEL = 10;

    // 작물 블록 -> 수확 경험치
    private static final Map<Material, Integer> CROP_XP = Map.ofEntries(
            Map.entry(Material.WHEAT, 3),
            Map.entry(Material.CARROTS, 3),
            Map.entry(Material.POTATOES, 3),
            Map.entry(Material.BEETROOTS, 3),
            Map.entry(Material.NETHER_WART, 4),
            Map.entry(Material.COCOA, 4),
            Map.entry(Material.PUMPKIN, 5),
            Map.entry(Material.MELON, 5),
            Map.entry(Material.SUGAR_CANE, 2),
            Map.entry(Material.BAMBOO, 1),
            Map.entry(Material.SWEET_BERRY_BUSH, 2),
            Map.entry(Material.CAVE_VINES, 2),
            Map.entry(Material.CAVE_VINES_PLANT, 2)
    );

    // 작물 블록 -> 추가 드랍 아이템
    private static final Map<Material, Material> EXTRA_DROPS = Map.ofEntries(
            Map.entry(Material.WHEAT, Material.WHEAT),
            Map.entry(Material.CARROTS, Material.CARROT),
            Map.entry(Material.POTATOES, Material.POTATO),
            Map.entry(Material.BEETROOTS, Material.BEETROOT),
            Map.entry(Material.NETHER_WART, Material.NETHER_WART),
            Map.entry(Material.COCOA, Material.COCOA_BEANS),
            Map.entry(Material.PUMPKIN, Material.PUMPKIN),
            Map.entry(Material.MELON, Material.MELON_SLICE),
            Map.entry(Material.SUGAR_CANE, Material.SUGAR_CANE),
            Map.entry(Material.BAMBOO, Material.BAMBOO),
            Map.entry(Material.SWEET_BERRY_BUSH, Material.SWEET_BERRIES),
            Map.entry(Material.CAVE_VINES, Material.GLOW_BERRIES),
            Map.entry(Material.CAVE_VINES_PLANT, Material.GLOW_BERRIES)
    );

    // 성장 단계(Ageable) 검사를 건너뛰는 작물 (사탕수수/대나무는 age가 성장도를 의미하지 않음)
    public static final Set<Material> SKIP_AGE_CHECK = Set.of(
            Material.SUGAR_CANE, Material.BAMBOO, Material.PUMPKIN, Material.MELON
    );

    private FarmingManager() {
    }

    public static boolean isCrop(Material material) {
        return CROP_XP.containsKey(material);
    }

    public static int getCropXp(Material material) {
        return CROP_XP.getOrDefault(material, 0);
    }

    public static Material getExtraDrop(Material material) {
        return EXTRA_DROPS.get(material);
    }

    /** 다음 레벨까지 필요한 경험치: (레벨+1)² x 100 (만렙 누적 38,500) */
    public static int xpToNext(int level) {
        return (level + 1) * (level + 1) * 100;
    }

    public static int getLevel(int totalXp) {
        int level = 0;
        int remain = totalXp;
        while (level < MAX_LEVEL && remain >= xpToNext(level)) {
            remain -= xpToNext(level);
            level++;
        }
        return level;
    }

    /** 현재 레벨 구간 안에서 채운 경험치 */
    public static int getXpIntoLevel(int totalXp) {
        int level = 0;
        int remain = totalXp;
        while (level < MAX_LEVEL && remain >= xpToNext(level)) {
            remain -= xpToNext(level);
            level++;
        }
        return remain;
    }

    /** 수확 시 추가 드랍 확률: 레벨 x 3% */
    public static double getExtraDropChance(int level) {
        return level * 0.03;
    }

    /** 추가 드랍 개수: 1~4레벨 1개, 5~9레벨 1~2개, 10레벨 2~3개 */
    public static int rollExtraDropAmount(int level) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (level >= MAX_LEVEL) return 2 + random.nextInt(2);
        if (level >= 5) return 1 + random.nextInt(2);
        return 1;
    }

    /** 경험치를 지급하고 레벨업 시 서버 전체에 알린다. 레벨업 여부를 반환. */
    public static boolean addXp(Player player, int xp) {
        UserDataFarming data = getUserData(player);
        int oldLevel = getLevel(data.getXp());
        data.addXp(xp);
        int newLevel = getLevel(data.getXp());
        if (newLevel <= oldLevel) {
            return false;
        }
        Bukkit.broadcast(Component.text().append(
                Component.text("[농사] ", NamedTextColor.GREEN),
                Component.text(player.getName(), NamedTextColor.AQUA),
                Component.text("님이 ", NamedTextColor.WHITE),
                Component.text("농사 " + newLevel + "레벨", NamedTextColor.GOLD),
                Component.text("을 달성했어요! 🎉", NamedTextColor.WHITE)
        ).build());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        return true;
    }

    public static UserDataFarming getUserData(Player player) {
        return BackasSurvivalPackExtended.getUserManager()
                .getUser(player)
                .getDataContainer()
                .getOrLoad(UserDataFarming.class);
    }
}
