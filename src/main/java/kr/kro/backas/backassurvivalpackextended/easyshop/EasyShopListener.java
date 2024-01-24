package kr.kro.backas.backassurvivalpackextended.easyshop;

import kr.kro.backas.backassurvivalpackextended.MoneyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EasyShopListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().title().equals(EasyShopInventory.INVENTORY_NAME)) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        int oldMoney = MoneyManager.getMoney(player);

        Inventory inventory = event.getInventory();
        List<ItemStack> returnedItems = new ArrayList<>();
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) {
                continue;
            }
            Material type = itemStack.getType();
            int cost = EasyShopInventory.getCost(type);
            if (cost == 0) {
                returnedItems.add(itemStack);
                continue;
            }
            if (itemStack.getItemMeta().hasDisplayName()) {
                returnedItems.add(itemStack);
                continue;
            }
            MoneyManager.addMoney(player, cost * itemStack.getAmount());
            player.sendMessage(Component.text().append(
                    Component.translatable(itemStack.translationKey()).color(NamedTextColor.GOLD),
                    Component.text(" (x" + itemStack.getAmount() + ") ", NamedTextColor.DARK_GRAY),
                    Component.text("을(를) ", NamedTextColor.GRAY),
                    Component.text(cost * itemStack.getAmount(), NamedTextColor.WHITE),
                    Component.text("원에 판매하였습니다.", NamedTextColor.GRAY)
            ));
        }
        boolean drop = false;
        for (ItemStack returnedItem : returnedItems) {
            HashMap<Integer, ItemStack> noSpaces = player.getInventory().addItem(returnedItem);
            if (drop) {
                Location dropLocation = player.getLocation();
                World dropWorld = player.getWorld();
                dropWorld.dropItem(dropLocation, returnedItem);
                continue;
            }
            if (!noSpaces.isEmpty()) {
                drop = true;
                player.sendMessage(Component.text().append(
                        Component.text("인벤토리에 공간이 부족하여 반환될 아이템이 바닥에 떨어졌습니다.", NamedTextColor.RED)
                ));
                Location dropLocation = player.getLocation();
                World dropWorld = player.getWorld();
                for (ItemStack toDrop : noSpaces.values()) {
                    dropWorld.dropItem(dropLocation, toDrop);
                }
            }
        }
        player.sendMessage(Component.text().append(
                Component.text("[판매완료] ", NamedTextColor.GREEN),
                Component.text(" 수익 ", NamedTextColor.WHITE),
                Component.text(" +" + (MoneyManager.getMoney(player) - oldMoney) + "원 ", NamedTextColor.YELLOW),
            Component.text("(" + oldMoney + "원 => " + MoneyManager.getMoney(player) + "원)", NamedTextColor.DARK_GRAY))
        );
    }
}
