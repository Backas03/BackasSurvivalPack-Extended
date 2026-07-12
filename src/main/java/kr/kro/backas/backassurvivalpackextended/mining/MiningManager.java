package kr.kro.backas.backassurvivalpackextended.mining;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMining;
import net.kyori.adventure.text.Component;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class MiningManager {

    public static final int MAX_LEVEL = 10;

    // 광석 블록 -> 채굴 경험치
    private static final Map<Material, Integer> ORE_XP = Map.ofEntries(
            Map.entry(Material.COAL_ORE, 2),
            Map.entry(Material.DEEPSLATE_COAL_ORE, 2),
            Map.entry(Material.COPPER_ORE, 2),
            Map.entry(Material.DEEPSLATE_COPPER_ORE, 2),
            Map.entry(Material.NETHER_QUARTZ_ORE, 2),
            Map.entry(Material.NETHER_GOLD_ORE, 2),
            Map.entry(Material.IRON_ORE, 3),
            Map.entry(Material.DEEPSLATE_IRON_ORE, 3),
            Map.entry(Material.REDSTONE_ORE, 3),
            Map.entry(Material.DEEPSLATE_REDSTONE_ORE, 3),
            Map.entry(Material.LAPIS_ORE, 3),
            Map.entry(Material.DEEPSLATE_LAPIS_ORE, 3),
            Map.entry(Material.GOLD_ORE, 4),
            Map.entry(Material.DEEPSLATE_GOLD_ORE, 4),
            Map.entry(Material.DIAMOND_ORE, 10),
            Map.entry(Material.DEEPSLATE_DIAMOND_ORE, 10),
            Map.entry(Material.EMERALD_ORE, 12),
            Map.entry(Material.DEEPSLATE_EMERALD_ORE, 12),
            Map.entry(Material.ANCIENT_DEBRIS, 20)
    );

    // 광석 블록 -> 추가 드랍 아이템
    private static final Map<Material, Material> EXTRA_DROPS = Map.ofEntries(
            Map.entry(Material.COAL_ORE, Material.COAL),
            Map.entry(Material.DEEPSLATE_COAL_ORE, Material.COAL),
            Map.entry(Material.COPPER_ORE, Material.RAW_COPPER),
            Map.entry(Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER),
            Map.entry(Material.NETHER_QUARTZ_ORE, Material.QUARTZ),
            Map.entry(Material.NETHER_GOLD_ORE, Material.GOLD_NUGGET),
            Map.entry(Material.IRON_ORE, Material.RAW_IRON),
            Map.entry(Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON),
            Map.entry(Material.REDSTONE_ORE, Material.REDSTONE),
            Map.entry(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE),
            Map.entry(Material.LAPIS_ORE, Material.LAPIS_LAZULI),
            Map.entry(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_LAZULI),
            Map.entry(Material.GOLD_ORE, Material.RAW_GOLD),
            Map.entry(Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD),
            Map.entry(Material.DIAMOND_ORE, Material.DIAMOND),
            Map.entry(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND),
            Map.entry(Material.EMERALD_ORE, Material.EMERALD),
            Map.entry(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD),
            Map.entry(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP)
    );

    private MiningManager() {
    }

    public static boolean isOre(Material material) {
        return ORE_XP.containsKey(material);
    }

    public static int getOreXp(Material material) {
        return ORE_XP.getOrDefault(material, 0);
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

    /** 채굴 시 추가 드랍 확률: 레벨 x 3% */
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
        UserDataMining data = getUserData(player);
        int oldLevel = getLevel(data.getXp());
        data.addXp(xp);
        int newLevel = getLevel(data.getXp());
        if (newLevel <= oldLevel) {
            return false;
        }
        Bukkit.broadcast(Component.text().append(
                Component.text("[광질] ", Palette.AQUA),
                Component.text(player.getName(), Palette.AQUA),
                Component.text("님이 ", Palette.WHITE),
                Component.text("광질 " + newLevel + "레벨", Palette.GOLD),
                Component.text("을 달성했어요! 🎉", Palette.WHITE)
        ).build());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        return true;
    }

    public static UserDataMining getUserData(Player player) {
        return BackasSurvivalPackExtended.getUserManager()
                .getUser(player)
                .getDataContainer()
                .getOrLoad(UserDataMining.class);
    }
}
