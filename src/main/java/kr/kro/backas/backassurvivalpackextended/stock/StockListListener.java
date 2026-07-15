package kr.kro.backas.backassurvivalpackextended.stock;

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

public class StockListListener implements Listener {

    private static final Map<UUID, Long> PENDING_SEARCH = new ConcurrentHashMap<>();
    private static final long SEARCH_TIMEOUT_MILLIS = 30_000;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(StockListInventory.INVENTORY_TITLE)) {
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
        StockListInventory.ViewState view = StockListInventory.getView(player);

        switch (slot) {
            case StockListInventory.TAB_KOSPI_SLOT -> {
                if (view.market() != StockMarket.KOSPI) {
                    StockListInventory.openAsync(player, StockMarket.KOSPI, 1, null);
                }
                return;
            }
            case StockListInventory.TAB_KOSDAQ_SLOT -> {
                if (view.market() != StockMarket.KOSDAQ) {
                    StockListInventory.openAsync(player, StockMarket.KOSDAQ, 1, null);
                }
                return;
            }
            case StockListInventory.TAB_NASDAQ_SLOT -> {
                if (view.market() != StockMarket.NASDAQ) {
                    StockListInventory.openAsync(player, StockMarket.NASDAQ, 1, null);
                }
                return;
            }
            case StockListInventory.PREV_PAGE_SLOT -> {
                if (event.getCurrentItem().getType() == Material.ARROW) {
                    StockListInventory.openAsync(player, view.market(), view.page() - 1, view.keyword());
                }
                return;
            }
            case StockListInventory.NEXT_PAGE_SLOT -> {
                if (event.getCurrentItem().getType() == Material.ARROW) {
                    StockListInventory.openAsync(player, view.market(), view.page() + 1, view.keyword());
                }
                return;
            }
            case StockListInventory.SEARCH_SLOT -> {
                if (event.getClick().isRightClick()) {
                    StockListInventory.openAsync(player, view.market(), 1, null);
                    return;
                }
                if (!event.getClick().isLeftClick()) return;
                PENDING_SEARCH.put(player.getUniqueId(), System.currentTimeMillis() + SEARCH_TIMEOUT_MILLIS);
                player.closeInventory();
                player.sendMessage(Component.text().append(
                        Component.text("[주식] ", Palette.GOLD),
                        Component.text(view.market().getDisplayName() + " 종목 검색어를 채팅에 입력해주세요. (30초 안에 입력, ", Palette.GRAY),
                        Component.text("취소", Palette.RED),
                        Component.text(" 입력 시 취소)", Palette.GRAY)
                ));
                return;
            }
            default -> {
            }
        }
        if (slot >= StockListInventory.ITEMS_PER_PAGE) {
            return;
        }

        List<Stock> entries = StockListInventory.currentEntries(view.market(), view.keyword());
        int index = (view.page() - 1) * StockListInventory.ITEMS_PER_PAGE + slot;
        if (index < 0 || index >= entries.size()) {
            return;
        }
        Stock stock = entries.get(index);

        if (event.getClick().isRightClick()) {
            BackasSurvivalPackExtended.getStockQuoteService().toggleLive(player, stock);
            return;
        }
        if (event.getClick().isLeftClick()) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
                try {
                    StockQuoteService.fetchDetail(stock);
                } catch (Exception ignored) {
                }
                StockQuoteService.sendDetail(player, stock);
            });
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
        StockMarket market = StockListInventory.getView(player).market();
        Bukkit.getScheduler().runTask(BackasSurvivalPackExtended.getInstance(),
                () -> StockListInventory.openAsync(player, market, 1, keyword));
    }
}
