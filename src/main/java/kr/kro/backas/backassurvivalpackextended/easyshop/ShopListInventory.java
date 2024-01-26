package kr.kro.backas.backassurvivalpackextended.easyshop;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopListInventory {
    public static final Component INVENTORY_NAME = Component.text("판매가능 아이템 목록")
            .decoration(TextDecoration.BOLD, true);

    public static Inventory newInventory() {
        Inventory inventory = Bukkit.createInventory(null, 54, INVENTORY_NAME);
        for (Map.Entry<Material, Integer> entry : EasyShopInventory.COSTS.entrySet()) {
            ItemStack item = new ItemStack(entry.getKey());
            item.editMeta(itemMeta -> {
                List<Component> lore = itemMeta.lore();
                if (lore == null) lore = new ArrayList<>();

                lore.add(Component.text().append(
                        Component.text("판매가격: ", NamedTextColor.WHITE),
                        Component.text(entry.getValue() + BackasSurvivalPackExtended.MONEY_UNIT, NamedTextColor.YELLOW)
                ).build().decoration(TextDecoration.ITALIC, false));

                itemMeta.lore(lore);
            });
            inventory.addItem(item);
        }
        return inventory;
    }
}
