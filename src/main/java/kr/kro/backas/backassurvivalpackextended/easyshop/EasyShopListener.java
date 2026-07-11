package kr.kro.backas.backassurvivalpackextended.easyshop;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.MoneyManager;
import kr.kro.backas.backassurvivalpackextended.easyshop.purchase.EasyPurchaseInventory;
import kr.kro.backas.backassurvivalpackextended.point.PointManager;
import kr.kro.backas.backassurvivalpackextended.point.title.Title;
import kr.kro.backas.backassurvivalpackextended.point.title.TitleManager;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataPoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EasyShopListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(EasyPurchaseInventory.INVENTORY_TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        int slot = event.getRawSlot();
        int page = EasyPurchaseInventory.getPage(player);
        if (slot >= EasyPurchaseInventory.TITLE_ROW_START) {
            handleTitleClick(player, event, slot);
            return;
        }
        if (slot == EasyPurchaseInventory.UNEQUIP_SLOT) {
            if (!event.getClick().isLeftClick()) return;
            TitleManager.unequip(player);
            player.sendMessage(Component.text("칭호를 해제하였습니다.", NamedTextColor.GRAY));
            EasyPurchaseInventory.open(player, page);
            return;
        }
        if (slot == EasyPurchaseInventory.PREV_PAGE_SLOT && event.getCurrentItem().getType() == Material.ARROW) {
            EasyPurchaseInventory.open(player, page - 1);
            return;
        }
        if (slot == EasyPurchaseInventory.NEXT_PAGE_SLOT && event.getCurrentItem().getType() == Material.ARROW) {
            EasyPurchaseInventory.open(player, page + 1);
            return;
        }
        if (slot >= EasyPurchaseInventory.ITEM_AREA_SIZE) {
            // 구분줄, 정보 아이템
            return;
        }

        int cost = EasyPurchaseInventory.getCost(slot, page);
        if (cost == 0) {
            player.sendMessage(Component.text("해당 아이템은 구매할 수 없습니다.", NamedTextColor.RED));
            return;
        }

        boolean byPoint = event.getClick().isRightClick();
        if (!event.getClick().isLeftClick() && !byPoint) {
            return;
        }

        int amount = event.getClick().isShiftClick() ? 64 : 1;

        if (byPoint) {
            if (PointManager.getPoint(player) < cost * amount) {
                player.sendMessage(Component.text("잠수 포인트가 부족합니다.", NamedTextColor.RED));
                player.closeInventory();
                return;
            }
        } else if (MoneyManager.getMoney(player) < cost * amount) {
            player.sendMessage(Component.text("돈이 부족합니다.", NamedTextColor.RED));
            player.closeInventory();
            return;
        }
        ItemStack item = EasyPurchaseInventory.getItem(slot, page);
        if (item == null) {
            player.sendMessage(Component.text("해당 아이템의 가격을 불러올 수 없습니다.", NamedTextColor.RED));
            return;
        }
        item = item.clone();
        item.setAmount(amount);
        HashMap<Integer, ItemStack> noSpaces = player.getInventory().addItem(item);
        if (!noSpaces.isEmpty()) {
            amount -= noSpaces.values().stream().mapToInt(ItemStack::getAmount).sum();
        }

        if (amount == 0) {
            player.sendMessage(Component.text("인벤토리에 공간이 부족합니다.", NamedTextColor.RED));
            player.closeInventory();
            return;
        }

        String unit = byPoint ? PointManager.POINT_UNIT : BackasSurvivalPackExtended.MONEY_UNIT;
        int oldBalance = byPoint ? PointManager.getPoint(player) : MoneyManager.getMoney(player);

        if (byPoint) {
            PointManager.removePoint(player, cost * amount);
        } else {
            MoneyManager.removeMoney(player, cost * amount, true);
        }
        int newBalance = byPoint ? PointManager.getPoint(player) : MoneyManager.getMoney(player);
        player.sendMessage(Component.text().append(
                Component.text("[구매완료] ", NamedTextColor.GREEN),
                Component.translatable(item.translationKey()).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD),
                Component.text(" (x" + amount + ") ", NamedTextColor.DARK_GRAY),
                Component.text("지출", NamedTextColor.GRAY),
                    Component.text(" -" + String.format("%,d", cost * amount) + unit + " ", NamedTextColor.RED),
                Component.text("(", NamedTextColor.GRAY),
                Component.text(String.format("%,d", oldBalance) + unit + " ", NamedTextColor.DARK_GRAY),
                Component.text("⮕ ", NamedTextColor.GRAY),
                Component.text(String.format("%,d", newBalance) + unit, NamedTextColor.WHITE),
                Component.text(")", NamedTextColor.GRAY))
        );
    }

    private void handleTitleClick(Player player, InventoryClickEvent event, int slot) {
        if (!event.getClick().isLeftClick()) {
            return;
        }
        Title title = EasyPurchaseInventory.getTitle(slot);
        if (title == null) {
            return;
        }
        int page = EasyPurchaseInventory.getPage(player);

        UserDataPoint data = PointManager.getUserDataPoint(player);
        if (title.name().equals(data.getEquippedTitle())) {
            TitleManager.unequip(player);
            player.sendMessage(Component.text("칭호를 해제하였습니다.", NamedTextColor.GRAY));
            EasyPurchaseInventory.open(player, page);
            return;
        }

        if (data.hasTitle(title.name())) {
            TitleManager.equip(player, title);
            player.sendMessage(Component.text().append(
                    title.display(),
                    Component.text(" 칭호를 장착하였습니다.", NamedTextColor.GREEN)
            ));
            EasyPurchaseInventory.open(player, page);
            return;
        }

        if (data.getAmount() < title.getCost()) {
            player.sendMessage(Component.text().append(
                    Component.text("잠수 포인트가 부족합니다. ", NamedTextColor.RED),
                    Component.text(String.format("(보유: %,d / 필요: %,d)", data.getAmount(), title.getCost()), NamedTextColor.GRAY)
            ));
            return;
        }

        data.subtract(title.getCost());
        data.addTitle(title.name());
        TitleManager.equip(player, title);
        player.sendMessage(Component.text().append(
                Component.text("[구매완료] ", NamedTextColor.GREEN),
                title.display(),
                Component.text(" 칭호를 구매하고 장착하였습니다. ", NamedTextColor.WHITE),
                Component.text("(-" + String.format("%,d", title.getCost()) + PointManager.POINT_UNIT + ")", NamedTextColor.GRAY)
        ));
        EasyPurchaseInventory.open(player, page);
    }

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
                    Component.translatable(itemStack.translationKey()).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD),
                    Component.text(" (x" + itemStack.getAmount() + ") ", NamedTextColor.DARK_GRAY),
                    Component.text("을(를) ", NamedTextColor.GRAY),
                    Component.text(cost * itemStack.getAmount(), NamedTextColor.YELLOW),
                    Component.text(BackasSurvivalPackExtended.MONEY_UNIT + "에 판매하였습니다.", NamedTextColor.GRAY)
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
                Component.text("수익", NamedTextColor.WHITE),
                Component.text(" +" + (MoneyManager.getMoney(player) - oldMoney) + BackasSurvivalPackExtended.MONEY_UNIT + " ", NamedTextColor.YELLOW),
                Component.text("(", NamedTextColor.GRAY),
                Component.text(oldMoney + BackasSurvivalPackExtended.MONEY_UNIT + " ", NamedTextColor.DARK_GRAY),
                Component.text("⮕ ", NamedTextColor.GRAY),
                Component.text(MoneyManager.getMoney(player) + BackasSurvivalPackExtended.MONEY_UNIT, NamedTextColor.WHITE),
                Component.text(")", NamedTextColor.GRAY))
        );
    }

    @EventHandler
    public void onShopListClick(InventoryClickEvent event) {
        if (event.getView().title().equals(ShopListInventory.INVENTORY_NAME)) {
            event.setCancelled(true);
        }
    }
}
