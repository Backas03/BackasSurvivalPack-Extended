package kr.kro.backas.backassurvivalpackextended.easyshop.purchase;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EasyPurchaseInventory {

    private static final Map<ItemStack, Integer> LIST_OF_ITEMS = new HashMap<>();

    public static final Component INVENTORY_TITLE = Component.text("상점").decorate(TextDecoration.BOLD);

    public static void addItem(ItemStack itemStack, int price) {
        LIST_OF_ITEMS.put(itemStack, price);
    }

    public static Inventory newInventory() {
        final int inventorySize = 54;

        Inventory inventory = Bukkit.createInventory(null, inventorySize, INVENTORY_TITLE);
        int slot = 0;
        for (Map.Entry<ItemStack, Integer> entry : LIST_OF_ITEMS.entrySet()) {
           ItemStack cloned = entry.getKey().clone();
           cloned.editMeta(itemMeta -> {
               List<Component> lore = itemMeta.lore();
               if (lore == null) lore = new ArrayList<>();

               lore.add(Component.empty());
               lore.add(Component.text().append(
                       Component.text("가격: ", NamedTextColor.WHITE),
                       Component.text(entry.getValue() + "원", NamedTextColor.YELLOW)
               ).build());
               lore.add(Component.empty());
               lore.add(Component.text("[좌클릭]", NamedTextColor.GREEN).append(
                       Component.text("1개 구매", NamedTextColor.WHITE)));
               lore.add(Component.text("[쉬프트 + 좌클릭]", NamedTextColor.GREEN).append(
                       Component.text("64개 구매", NamedTextColor.WHITE)));

               itemMeta.lore(lore);
           });
           inventory.setItem(slot++, cloned);
           if (slot >= inventorySize) break;
        }
        return inventory;
    }

    public static int getCost(int slot) {
        ItemStack item = getItem(slot);
        if (item == null) return 0;
        return LIST_OF_ITEMS.getOrDefault(item, 0);
    }

    public static ItemStack getItem(int slot) {
        List<ItemStack> items = new ArrayList<>(LIST_OF_ITEMS.keySet());
        if (items.size() <= slot) return null;
        return items.get(slot);
    }
}
