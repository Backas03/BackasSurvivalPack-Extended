package kr.kro.backas.backassurvivalpackextended.coin;

import io.papermc.paper.event.player.AsyncChatEvent;
import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CoinListListener implements Listener {

    // 검색어 입력 대기 중인 플레이어 (값: 만료 시각 millis)
    private static final Map<UUID, Long> PENDING_SEARCH = new ConcurrentHashMap<>();
    private static final long SEARCH_TIMEOUT_MILLIS = 30_000;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(CoinListInventory.INVENTORY_TITLE)) {
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

        int slot = event.getRawSlot();
        CoinListInventory.ViewState view = CoinListInventory.getView(player);

        if (slot == CoinListInventory.PREV_PAGE_SLOT && event.getCurrentItem().getType() == Material.ARROW) {
            CoinListInventory.open(player, view.page() - 1, view.keyword());
            return;
        }
        if (slot == CoinListInventory.NEXT_PAGE_SLOT && event.getCurrentItem().getType() == Material.ARROW) {
            CoinListInventory.open(player, view.page() + 1, view.keyword());
            return;
        }
        if (slot == CoinListInventory.SEARCH_SLOT) {
            if (event.getClick().isRightClick()) {
                CoinListInventory.open(player, 1, null);
                return;
            }
            if (!event.getClick().isLeftClick()) return;
            PENDING_SEARCH.put(player.getUniqueId(), System.currentTimeMillis() + SEARCH_TIMEOUT_MILLIS);
            player.closeInventory();
            player.sendMessage(Component.text().append(
                    Component.text("[코인] ", Palette.GOLD),
                    Component.text("검색어를 채팅에 입력해주세요. (30초 안에 입력, ", Palette.GRAY),
                    Component.text("취소", Palette.RED),
                    Component.text(" 입력 시 취소)", Palette.GRAY)
            ));
            return;
        }
        if (slot >= CoinListInventory.ITEMS_PER_PAGE) {
            return;
        }

        List<UpbitMarket> entries = CoinListInventory.currentEntries(view.keyword());
        int index = (view.page() - 1) * CoinListInventory.ITEMS_PER_PAGE + slot;
        if (index < 0 || index >= entries.size()) {
            return;
        }
        UpbitMarket market = entries.get(index);

        if (event.getClick().isRightClick()) {
            BackasSurvivalPackExtended.getCoinService().toggleLive(player, market);
            return;
        }
        if (event.getClick().isLeftClick()) {
            player.closeInventory();
            CoinService.sendDetail(player, market);
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Long expiry = PENDING_SEARCH.remove(player.getUniqueId());
        if (expiry == null) {
            return;
        }
        event.setCancelled(true);
        if (expiry < System.currentTimeMillis()) {
            player.sendMessage(Component.text("검색 입력 시간이 지났습니다. 다시 시도해주세요.", Palette.RED));
            return;
        }
        String keyword = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        if (keyword.equals("취소") || keyword.isEmpty()) {
            player.sendMessage(Component.text("검색을 취소했습니다.", Palette.GRAY));
            return;
        }
        // GUI는 메인 스레드에서 열어야 한다
        Bukkit.getScheduler().runTask(BackasSurvivalPackExtended.getInstance(),
                () -> CoinListInventory.open(player, 1, keyword));
    }
}
