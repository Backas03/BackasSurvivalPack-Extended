package kr.kro.backas.backassurvivalpackextended.menu;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class MenuListener implements Listener {

    // 웅크리기 + F(양손 교체) -> 메뉴 열기
    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (!event.getPlayer().isSneaking()) return;
        event.setCancelled(true);
        MenuInventory.open(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(MenuInventory.INVENTORY_TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        if (!event.getClick().isLeftClick()) {
            return;
        }
        String command = MenuInventory.getCommand(event.getRawSlot());
        if (command == null) {
            return;
        }
        player.closeInventory();
        // 인벤토리 클릭 이벤트 처리 중에 다른 GUI를 여는 충돌을 피하기 위해 다음 틱에 실행
        Bukkit.getScheduler().runTask(BackasSurvivalPackExtended.getInstance(),
                () -> player.performCommand(command));
    }
}
