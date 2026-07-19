package kr.kro.backas.backassurvivalpackextended.easyshop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EasyShopInventory {
    public static final LinkedHashMap<Material, Integer> COSTS = new LinkedHashMap<>();

    static {
        COSTS.put(Material.NETHERITE_INGOT, 100000);
        COSTS.put(Material.DIAMOND, 10000);
        COSTS.put(Material.REDSTONE, 15);
        COSTS.put(Material.GRASS_BLOCK, 5);
        COSTS.put(Material.DIRT, 5);
        COSTS.put(Material.COBBLESTONE, 5);
        COSTS.put(Material.STONE, 5);
        COSTS.put(Material.DEEPSLATE, 5);
        COSTS.put(Material.COBBLED_DEEPSLATE, 5);
        COSTS.put(Material.GRAVEL, 5);
        COSTS.put(Material.TUFF, 4);
        COSTS.put(Material.NETHERRACK, 2);
        COSTS.put(Material.BLACKSTONE, 6);
        COSTS.put(Material.POTATO, 25);
        COSTS.put(Material.POISONOUS_POTATO, 500);
        COSTS.put(Material.CAKE, 2500);
        COSTS.put(Material.CARROT, 27);
        COSTS.put(Material.GOLDEN_CARROT, 42);
        COSTS.put(Material.WHEAT, 70);
        COSTS.put(Material.BEETROOT, 70);
        COSTS.put(Material.PUMPKIN, 36);
        COSTS.put(Material.MELON, 36);
        COSTS.put(Material.MELON_SLICE, 5);
        COSTS.put(Material.ROTTEN_FLESH, 2);
        COSTS.put(Material.SADDLE, 1000);
        COSTS.put(Material.TOTEM_OF_UNDYING, 500);
        COSTS.put(Material.SUSPICIOUS_STEW, 1250);
        COSTS.put(Material.COOKIE, 20);
        // 요리류: 재료값 + 가공 보너스 (생고기는 팔 수 없음 - 구워야 값이 나감)
        COSTS.put(Material.BAKED_POTATO, 45);
        COSTS.put(Material.BREAD, 250);
        COSTS.put(Material.COOKED_BEEF, 60);
        COSTS.put(Material.COOKED_PORKCHOP, 60);
        COSTS.put(Material.COOKED_CHICKEN, 45);
        COSTS.put(Material.COOKED_MUTTON, 50);
        COSTS.put(Material.COOKED_RABBIT, 55);
        COSTS.put(Material.COOKED_COD, 45);
        COSTS.put(Material.COOKED_SALMON, 60);
        COSTS.put(Material.PUMPKIN_PIE, 180);
        COSTS.put(Material.MUSHROOM_STEW, 120);
        COSTS.put(Material.RABBIT_STEW, 400);
        COSTS.put(Material.DRIED_KELP_BLOCK, 30);
    }

    public static final Component INVENTORY_NAME = Component.text("판매하려는 아이템을 넣고 창을 닫아주세요")
            .decorate(TextDecoration.BOLD);

    public static Inventory newInventory() {
        return Bukkit.createInventory(null, 27, INVENTORY_NAME);
    }

    public static int getCost(Material material) {
        return COSTS.getOrDefault(material, 0);
    }
}
