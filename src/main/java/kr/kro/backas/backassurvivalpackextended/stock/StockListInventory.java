package kr.kro.backas.backassurvivalpackextended.stock;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.coin.CoinService;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 주식 시세 GUI. 코스피/코스닥은 시총순, 나스닥은 유명 종목 큐레이션 + 검색.
 */
public final class StockListInventory {

    public static final Component INVENTORY_TITLE = Component.text("주식 시세").decorate(TextDecoration.BOLD);
    public static final int INVENTORY_SIZE = 54;
    public static final int ITEMS_PER_PAGE = 45;
    public static final int TAB_KOSPI_SLOT = 45;
    public static final int TAB_KOSDAQ_SLOT = 46;
    public static final int TAB_NASDAQ_SLOT = 47;
    public static final int PREV_PAGE_SLOT = 48;
    public static final int SEARCH_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 50;

    public record ViewState(StockMarket market, int page, @Nullable String keyword) {
    }

    private static final Map<UUID, ViewState> VIEWS = new HashMap<>();

    private StockListInventory() {
    }

    public static List<Stock> currentEntries(StockMarket market, @Nullable String keyword) {
        return keyword == null ? StockRegistry.getFeatured(market) : StockRegistry.search(market, keyword);
    }

    /** 시세를 비동기로 가져온 뒤 메인 스레드에서 GUI를 연다. */
    public static void openAsync(Player player, StockMarket market, int page, @Nullable String keyword) {
        if (!StockRegistry.isLoaded()) {
            player.sendMessage(Component.text("아직 종목 목록을 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Palette.RED));
            return;
        }
        List<Stock> entries = currentEntries(market, keyword);
        if (entries.isEmpty()) {
            player.sendMessage(Component.text().append(
                    Component.text("[주식] ", Palette.GOLD),
                    Component.text("검색 결과가 없습니다: ", Palette.GRAY),
                    Component.text(keyword == null ? "" : keyword, Palette.WHITE)
            ));
            return;
        }
        int maxPage = Math.max(1, (int) Math.ceil((double) entries.size() / ITEMS_PER_PAGE));
        int clampedPage = Math.max(1, Math.min(page, maxPage));
        int startIndex = (clampedPage - 1) * ITEMS_PER_PAGE;
        List<Stock> pageStocks = entries.subList(startIndex, Math.min(startIndex + ITEMS_PER_PAGE, entries.size()));

        Bukkit.getScheduler().runTaskAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
            try {
                StockQuoteService.fetchQuotes(pageStocks);
            } catch (Exception ignored) {
                // 실패해도 캐시된 값으로 연다
            }
            Bukkit.getScheduler().runTask(BackasSurvivalPackExtended.getInstance(),
                    () -> open(player, market, clampedPage, maxPage, keyword, pageStocks));
        });
    }

    private static void open(Player player, StockMarket market, int page, int maxPage,
                             @Nullable String keyword, List<Stock> pageStocks) {
        if (!player.isOnline()) return;
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);

        for (int i = 0; i < pageStocks.size(); i++) {
            inventory.setItem(i, createStockItem(pageStocks.get(i)));
        }

        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        pane.editMeta(meta -> meta.displayName(Component.empty()));
        for (int i = ITEMS_PER_PAGE; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, pane);
        }
        inventory.setItem(TAB_KOSPI_SLOT, createTabItem(StockMarket.KOSPI, market));
        inventory.setItem(TAB_KOSDAQ_SLOT, createTabItem(StockMarket.KOSDAQ, market));
        inventory.setItem(TAB_NASDAQ_SLOT, createTabItem(StockMarket.NASDAQ, market));
        if (page > 1) {
            inventory.setItem(PREV_PAGE_SLOT, createArrow("이전 페이지", page - 1, maxPage));
        }
        if (page < maxPage) {
            inventory.setItem(NEXT_PAGE_SLOT, createArrow("다음 페이지", page + 1, maxPage));
        }
        inventory.setItem(SEARCH_SLOT, createSearchItem(market, keyword, page, maxPage));

        player.openInventory(inventory);
        VIEWS.put(player.getUniqueId(), new ViewState(market, page, keyword));
    }

    public static ViewState getView(Player player) {
        return VIEWS.getOrDefault(player.getUniqueId(), new ViewState(StockMarket.KOSPI, 1, null));
    }

    private static ItemStack createStockItem(Stock stock) {
        StockQuote quote = StockQuoteService.getCached(stock);
        double rate = quote == null ? 0 : quote.changeRate();
        Material icon = rate > 0 ? Material.RED_CANDLE
                : rate < 0 ? Material.BLUE_CANDLE
                : Material.WHITE_CANDLE;

        ItemStack item = new ItemStack(icon);
        item.editMeta(meta -> {
            meta.displayName(noItalic(Component.text(
                    stock.name() + " (" + stock.code() + ")", StockQuoteService.color(rate))));
            List<Component> lore = new ArrayList<>();
            if (quote == null) {
                lore.add(noItalic(Component.text("시세를 불러오지 못했습니다.", Palette.GRAY)));
            } else {
                lore.add(noItalic(Component.text("현재가: ", Palette.GRAY)
                        .append(Component.text(stock.market().formatMoney(
                                CoinService.formatPrice(quote.price())), Palette.WHITE))));
                lore.add(noItalic(Component.text("전일 대비: ", Palette.GRAY)
                        .append(Component.text(StockQuoteService.arrow(rate) + " "
                                        + String.format("%.2f%%", Math.abs(rate))
                                        + " (" + StockQuoteService.signed(quote.changeAmount(), stock.market()) + ")",
                                StockQuoteService.color(rate)))));
                if (quote.high() >= 0 && quote.low() >= 0) {
                    lore.add(noItalic(Component.text("금일 고가/저가: ", Palette.GRAY)
                            .append(Component.text(CoinService.formatPrice(quote.high()), Palette.RED))
                            .append(Component.text(" / ", Palette.GRAY))
                            .append(Component.text(CoinService.formatPrice(quote.low()), Palette.BLUE))));
                }
                if (quote.tradingValue() >= 0) {
                    lore.add(noItalic(Component.text("거래대금: ", Palette.GRAY)
                            .append(Component.text(CoinService.formatVolume(quote.tradingValue()) + "원", Palette.YELLOW))));
                }
                if (quote.marketOpen() != null) {
                    lore.add(noItalic(Component.text(quote.marketOpen() ? "🟢 장중" : "⚪ 장마감", Palette.GRAY)));
                }
            }
            lore.add(Component.empty());
            lore.add(noItalic(Component.text("[좌클릭]", Palette.GREEN)
                    .append(Component.text(" 상세 보기", Palette.WHITE))));
            lore.add(noItalic(Component.text("[우클릭]", Palette.AQUA)
                    .append(Component.text(" LIVE 액션바 알림 토글", Palette.WHITE))));
            meta.lore(lore);
        });
        return item;
    }

    private static ItemStack createTabItem(StockMarket tab, StockMarket selected) {
        ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> {
            meta.displayName(noItalic(Component.text(tab.getDisplayName(),
                    tab == selected ? Palette.YELLOW : Palette.GRAY)));
            meta.lore(List.of(noItalic(Component.text(
                    tab == selected ? "현재 보고 있는 시장" : "[좌클릭] 이 시장 보기", Palette.GRAY))));
            if (tab == selected) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        });
        return item;
    }

    private static ItemStack createArrow(String name, int targetPage, int maxPage) {
        ItemStack item = new ItemStack(Material.ARROW);
        item.editMeta(meta -> {
            meta.displayName(noItalic(Component.text(name, Palette.GREEN)));
            meta.lore(List.of(
                    noItalic(Component.text("[좌클릭] " + targetPage + "/" + maxPage + " 페이지로 이동", Palette.GRAY))
            ));
        });
        return item;
    }

    private static ItemStack createSearchItem(StockMarket market, @Nullable String keyword, int page, int maxPage) {
        ItemStack item = new ItemStack(Material.COMPASS);
        item.editMeta(meta -> {
            meta.displayName(noItalic(Component.text("종목 검색 (" + market.getDisplayName() + ")", Palette.YELLOW)));
            List<Component> lore = new ArrayList<>();
            if (keyword != null) {
                lore.add(noItalic(Component.text("현재 검색어: ", Palette.GRAY)
                        .append(Component.text(keyword, Palette.WHITE))));
            } else if (market == StockMarket.NASDAQ) {
                lore.add(noItalic(Component.text("기본 목록은 인기 종목입니다. 전체는 검색으로!", Palette.GRAY)));
            }
            lore.add(noItalic(Component.text("페이지: " + page + "/" + maxPage, Palette.GRAY)));
            lore.add(Component.empty());
            lore.add(noItalic(Component.text("[좌클릭]", Palette.GREEN)
                    .append(Component.text(" 검색어 입력 (채팅으로)", Palette.WHITE))));
            lore.add(noItalic(Component.text("[우클릭]", Palette.AQUA)
                    .append(Component.text(" 기본 목록 보기", Palette.WHITE))));
            meta.lore(lore);
        });
        return item;
    }

    private static Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
