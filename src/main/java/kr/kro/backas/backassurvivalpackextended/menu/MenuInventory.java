package kr.kro.backas.backassurvivalpackextended.menu;

import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 웅크린 상태에서 F(양손 교체) 키를 누르면 열리는 메인 메뉴.
 */
public final class MenuInventory {

    public static final Component INVENTORY_TITLE = Component.text("메뉴").decorate(TextDecoration.BOLD);
    public static final int INVENTORY_SIZE = 27;
    private static final int START_SLOT = 9;

    private record Entry(Material icon, String name, TextColor color, String description, String command) {
    }

    private static final List<Entry> ENTRIES = List.of(
            new Entry(Material.EMERALD, "상점", Palette.GREEN, "아이템 구매 (돈/잠수 포인트) · 칭호", "shop"),
            new Entry(Material.CHEST, "판매", Palette.GOLD, "아이템을 넣고 닫으면 판매됩니다", "sell"),
            new Entry(Material.ENDER_PEARL, "워프", Palette.PURPLE, "워프 목록 열기", "warp"),
            new Entry(Material.GOLD_INGOT, "랭킹", Palette.YELLOW, "각종 랭킹 확인", "ranking"),
            new Entry(Material.EXPERIENCE_BOTTLE, "잠수 포인트", Palette.AQUA, "보유 잠수 포인트 확인", "point"),
            new Entry(Material.WHEAT, "농사 레벨", Palette.GREEN, "농사 레벨/경험치/특전 확인", "farming"),
            new Entry(Material.IRON_PICKAXE, "광질 레벨", Palette.AQUA, "광질 레벨/경험치/특전 확인", "mining"),
            new Entry(Material.SUNFLOWER, "코인 시세", Palette.GOLD, "전체 코인 시세 GUI · 등락률/검색", "coin")
    );

    private MenuInventory() {
    }

    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);

        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        pane.editMeta(meta -> meta.displayName(Component.empty()));
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, pane);
        }

        for (int i = 0; i < ENTRIES.size(); i++) {
            Entry entry = ENTRIES.get(i);
            ItemStack item = new ItemStack(entry.icon());
            item.editMeta(meta -> {
                meta.displayName(Component.text(entry.name(), entry.color())
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(List.of(
                        Component.text(entry.description(), Palette.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("[좌클릭]", Palette.GREEN)
                                .append(Component.text(" 열기", Palette.WHITE))
                                .decoration(TextDecoration.ITALIC, false)
                ));
            });
            inventory.setItem(START_SLOT + i, item);
        }

        player.openInventory(inventory);
    }

    @Nullable
    public static String getCommand(int slot) {
        int index = slot - START_SLOT;
        if (index < 0 || index >= ENTRIES.size()) return null;
        return ENTRIES.get(index).command();
    }
}
