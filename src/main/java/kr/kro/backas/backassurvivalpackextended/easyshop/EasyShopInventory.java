package kr.kro.backas.backassurvivalpackextended.easyshop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class EasyShopInventory {
    private static final Map<Material, Integer> COSTS = new HashMap<>();

    static {
        COSTS.put(Material.DIAMOND, 10000);
        COSTS.put(Material.DIRT, 5);
        COSTS.put(Material.GRASS_BLOCK, 5);
        COSTS.put(Material.NETHERITE_INGOT, 100000);
    }

    public static final Component INVENTORY_NAME = Component.text("판매하려는 아이템을 넣고 창을 닫아주세요")
            .decorate(TextDecoration.BOLD);

    public static Inventory newInventory() {
        return Bukkit.createInventory(null, 9, INVENTORY_NAME);
    }

    public static int getCost(Material material) {
        return COSTS.getOrDefault(material, 0);
    }
}
