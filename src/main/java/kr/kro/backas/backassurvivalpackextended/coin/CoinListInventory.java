package kr.kro.backas.backassurvivalpackextended.coin;

import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 코인 시세 리스트 GUI. 24시간 거래대금 순으로 정렬되며 페이지/검색을 지원한다.
 */
public final class CoinListInventory {

    public static final Component INVENTORY_TITLE = Component.text("코인 시세").decorate(TextDecoration.BOLD);
    public static final int INVENTORY_SIZE = 54;
    public static final int ITEMS_PER_PAGE = 45;
    public static final int PREV_PAGE_SLOT = 45;
    public static final int SEARCH_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 53;

    public record ViewState(int page, @Nullable String keyword) {
    }

    // 플레이어가 현재 보고 있는 페이지/검색어
    private static final Map<UUID, ViewState> VIEWS = new HashMap<>();

    private CoinListInventory() {
    }

    /** 표시 목록: 검색어가 있으면 검색 결과, 없으면 전체 — 24h 거래대금 내림차순 */
    public static List<UpbitMarket> currentEntries(@Nullable String keyword) {
        List<UpbitMarket> entries = keyword == null
                ? new ArrayList<>(MarketRegistry.getAll())
                : MarketRegistry.search(keyword);
        entries.removeIf(market -> TickerCache.get(market.getMarket()) == null);
        entries.sort(Comparator.comparingDouble(
                (UpbitMarket market) -> TickerCache.get(market.getMarket()).getAccTradePrice24h()
        ).reversed());
        return entries;
    }

    public static void open(Player player, int page, @Nullable String keyword) {
        if (!MarketRegistry.isLoaded() || !TickerCache.isLoaded()) {
            player.sendMessage(Component.text("아직 시세 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Palette.RED));
            return;
        }
        List<UpbitMarket> entries = currentEntries(keyword);
        if (entries.isEmpty()) {
            player.sendMessage(Component.text().append(
                    Component.text("[코인] ", Palette.GOLD),
                    Component.text("검색 결과가 없습니다: ", Palette.GRAY),
                    Component.text(keyword == null ? "" : keyword, Palette.WHITE)
            ));
            return;
        }

        int maxPage = Math.max(1, (int) Math.ceil((double) entries.size() / ITEMS_PER_PAGE));
        page = Math.max(1, Math.min(page, maxPage));

        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE && startIndex + i < entries.size(); i++) {
            UpbitMarket market = entries.get(startIndex + i);
            UpbitTicker ticker = TickerCache.get(market.getMarket());
            if (ticker == null) continue;
            inventory.setItem(i, createCoinItem(market, ticker));
        }

        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        pane.editMeta(meta -> meta.displayName(Component.empty()));
        for (int i = ITEMS_PER_PAGE; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, pane);
        }
        if (page > 1) {
            inventory.setItem(PREV_PAGE_SLOT, createArrow("이전 페이지", page - 1, maxPage));
        }
        if (page < maxPage) {
            inventory.setItem(NEXT_PAGE_SLOT, createArrow("다음 페이지", page + 1, maxPage));
        }
        inventory.setItem(SEARCH_SLOT, createSearchItem(keyword, page, maxPage));

        player.openInventory(inventory);
        VIEWS.put(player.getUniqueId(), new ViewState(page, keyword));
    }

    public static ViewState getView(Player player) {
        return VIEWS.getOrDefault(player.getUniqueId(), new ViewState(1, null));
    }

    private static ItemStack createCoinItem(UpbitMarket market, UpbitTicker ticker) {
        double rate = ticker.getSignedChangeRate();
        Material icon = rate > 0 ? Material.RED_CANDLE
                : rate < 0 ? Material.BLUE_CANDLE
                : Material.WHITE_CANDLE;

        ItemStack item = new ItemStack(icon);
        item.editMeta(meta -> {
            meta.displayName(noItalic(Component.text(
                    market.getKoreanName() + " (" + market.getSymbol() + ")",
                    CoinService.changeColor(rate))));
            meta.lore(List.of(
                    noItalic(Component.text("현재가: ", Palette.GRAY)
                            .append(Component.text(CoinService.formatPrice(ticker.getTradePrice()) + "원", Palette.WHITE))),
                    noItalic(Component.text("전일 대비: ", Palette.GRAY)
                            .append(Component.text(CoinService.changeArrow(rate) + " " + CoinService.formatRate(rate)
                                    + " (" + CoinService.signedPrice(ticker.getSignedChangePrice()) + "원)",
                                    CoinService.changeColor(rate)))),
                    noItalic(Component.text("24h 고가/저가: ", Palette.GRAY)
                            .append(Component.text(CoinService.formatPrice(ticker.getHighPrice()), Palette.RED))
                            .append(Component.text(" / ", Palette.GRAY))
                            .append(Component.text(CoinService.formatPrice(ticker.getLowPrice()), Palette.BLUE))),
                    noItalic(Component.text("24h 거래대금: ", Palette.GRAY)
                            .append(Component.text(CoinService.formatVolume(ticker.getAccTradePrice24h()) + "원", Palette.YELLOW))),
                    Component.empty(),
                    noItalic(Component.text("[좌클릭]", Palette.GREEN)
                            .append(Component.text(" 상세 보기", Palette.WHITE))),
                    noItalic(Component.text("[우클릭]", Palette.AQUA)
                            .append(Component.text(" LIVE 액션바 알림 토글", Palette.WHITE)))
            ));
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

    private static ItemStack createSearchItem(@Nullable String keyword, int page, int maxPage) {
        ItemStack item = new ItemStack(Material.COMPASS);
        item.editMeta(meta -> {
            meta.displayName(noItalic(Component.text("코인 검색", Palette.YELLOW)));
            List<Component> lore = new ArrayList<>();
            if (keyword != null) {
                lore.add(noItalic(Component.text("현재 검색어: ", Palette.GRAY)
                        .append(Component.text(keyword, Palette.WHITE))));
            }
            lore.add(noItalic(Component.text("페이지: " + page + "/" + maxPage, Palette.GRAY)));
            lore.add(Component.empty());
            lore.add(noItalic(Component.text("[좌클릭]", Palette.GREEN)
                    .append(Component.text(" 검색어 입력 (채팅으로)", Palette.WHITE))));
            lore.add(noItalic(Component.text("[우클릭]", Palette.AQUA)
                    .append(Component.text(" 전체 목록 보기", Palette.WHITE))));
            meta.lore(lore);
        });
        return item;
    }

    private static Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
