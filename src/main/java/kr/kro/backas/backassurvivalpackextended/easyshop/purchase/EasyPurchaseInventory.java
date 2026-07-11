package kr.kro.backas.backassurvivalpackextended.easyshop.purchase;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.MoneyManager;
import kr.kro.backas.backassurvivalpackextended.easyshop.ItemFactory;
import kr.kro.backas.backassurvivalpackextended.point.PointManager;
import kr.kro.backas.backassurvivalpackextended.point.title.Title;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataPoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EasyPurchaseInventory {

    private static final LinkedHashMap<ItemStack, Integer> LIST_OF_ITEMS = new LinkedHashMap<>();

    static {
        LIST_OF_ITEMS.put(new ItemStack(Material.VILLAGER_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.MAGMA_CUBE_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.HOGLIN_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.WOLF_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.SHEEP_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.MOOSHROOM_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.COW_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.ZOMBIE_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.BEE_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.COPPER_GOLEM_SPAWN_EGG), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.WITHER_SKELETON_SKULL), 150000);
        LIST_OF_ITEMS.put(new ItemStack(Material.END_CRYSTAL), 75000);
        LIST_OF_ITEMS.put(new ItemStack(Material.NAME_TAG), 120000);
        LIST_OF_ITEMS.put(new ItemStack(Material.RABBIT_FOOT), 5000);
        LIST_OF_ITEMS.put(new ItemStack(Material.LEAD), 10000);
        LIST_OF_ITEMS.put(new ItemStack(Material.SLIME_BALL), 20000);
        LIST_OF_ITEMS.put(new ItemStack(Material.GHAST_TEAR), 10000);
        LIST_OF_ITEMS.put(new ItemStack(Material.MAGMA_CREAM), 15000);
        LIST_OF_ITEMS.put(new ItemStack(Material.SHULKER_SHELL), 50000);
        LIST_OF_ITEMS.put(new ItemStack(Material.PHANTOM_MEMBRANE), 15000);
        LIST_OF_ITEMS.put(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 25000);
        LIST_OF_ITEMS.put(new ItemStack(Material.OCHRE_FROGLIGHT), 2000);
        LIST_OF_ITEMS.put(new ItemStack(Material.VERDANT_FROGLIGHT), 2000);
        LIST_OF_ITEMS.put(new ItemStack(Material.PEARLESCENT_FROGLIGHT), 2000);
        LIST_OF_ITEMS.put(new ItemStack(Material.DIAMOND), 20000);
        LIST_OF_ITEMS.put(new ItemStack(Material.FIREWORK_ROCKET), 75);
        LIST_OF_ITEMS.put(new ItemStack(Material.NETHERITE_INGOT), 300000);
        LIST_OF_ITEMS.put(new ItemStack(Material.ELYTRA), 1250000);
        LIST_OF_ITEMS.put(ItemFactory.getHead(), 200000);
        LIST_OF_ITEMS.put(ItemFactory.getTPCoolTimeClear(), 10000);
        LIST_OF_ITEMS.put(new ItemStack(Material.WHITE_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.LIGHT_GRAY_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.GRAY_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.BLACK_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.BROWN_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.RED_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.ORANGE_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.YELLOW_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.LIME_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.GREEN_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.CYAN_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.LIGHT_BLUE_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.BLUE_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.PURPLE_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.MAGENTA_DYE), 500);
        LIST_OF_ITEMS.put(new ItemStack(Material.PINK_DYE), 500);
    }

    public static final Component INVENTORY_TITLE = Component.text("상점").decorate(TextDecoration.BOLD);

    public static final int INVENTORY_SIZE = 54;
    // 0~35: 아이템, 36~44: 구분줄(페이지 이동/정보/칭호해제 포함), 45~53: 칭호
    public static final int ITEM_AREA_SIZE = 36;
    public static final int PREV_PAGE_SLOT = 36;
    public static final int INFO_SLOT = 40;
    public static final int NEXT_PAGE_SLOT = 43;
    public static final int UNEQUIP_SLOT = 44;
    public static final int TITLE_ROW_START = 45;

    // 플레이어가 현재 열고 있는 상점 페이지
    private static final Map<UUID, Integer> OPEN_PAGES = new HashMap<>();

    public static void addItem(ItemStack itemStack, int price) {
        LIST_OF_ITEMS.put(itemStack, price);
    }

    public static void open(Player player) {
        open(player, 1);
    }

    public static void open(Player player, int page) {
        int maxPage = getMaxPage();
        page = Math.max(1, Math.min(page, maxPage));

        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);
        UserDataPoint pointData = PointManager.getUserDataPoint(player);

        int slot = 0;
        int startIndex = (page - 1) * ITEM_AREA_SIZE;
        List<Map.Entry<ItemStack, Integer>> entries = new ArrayList<>(LIST_OF_ITEMS.entrySet());
        for (int i = startIndex; i < entries.size() && slot < ITEM_AREA_SIZE; i++) {
            Map.Entry<ItemStack, Integer> entry = entries.get(i);
            inventory.setItem(slot++, createShopItem(entry.getKey(), entry.getValue()));
        }

        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        pane.editMeta(meta -> meta.displayName(Component.empty()));
        for (int i = ITEM_AREA_SIZE; i < TITLE_ROW_START; i++) {
            inventory.setItem(i, pane);
        }
        if (page > 1) {
            inventory.setItem(PREV_PAGE_SLOT, createPageArrow("이전 페이지", page - 1, maxPage));
        }
        if (page < maxPage) {
            inventory.setItem(NEXT_PAGE_SLOT, createPageArrow("다음 페이지", page + 1, maxPage));
        }
        inventory.setItem(INFO_SLOT, createInfoItem(player, pointData));
        inventory.setItem(UNEQUIP_SLOT, createUnequipItem());

        Title[] titles = Title.values();
        for (int i = 0; i < titles.length && TITLE_ROW_START + i < INVENTORY_SIZE; i++) {
            inventory.setItem(TITLE_ROW_START + i, createTitleItem(titles[i], pointData));
        }

        player.openInventory(inventory);
        OPEN_PAGES.put(player.getUniqueId(), page);
    }

    public static int getPage(Player player) {
        return OPEN_PAGES.getOrDefault(player.getUniqueId(), 1);
    }

    public static int getMaxPage() {
        return Math.max(1, (int) Math.ceil((double) LIST_OF_ITEMS.size() / ITEM_AREA_SIZE));
    }

    private static ItemStack createPageArrow(String name, int targetPage, int maxPage) {
        ItemStack item = new ItemStack(Material.ARROW);
        item.editMeta(meta -> {
            meta.displayName(noItalic(Component.text(name, NamedTextColor.GREEN)));
            meta.lore(List.of(
                    noItalic(Component.text("[좌클릭] " + targetPage + "/" + maxPage + " 페이지로 이동", NamedTextColor.GRAY))
            ));
        });
        return item;
    }

    private static ItemStack createShopItem(ItemStack itemStack, int cost) {
        ItemStack cloned = itemStack.clone();
        cloned.editMeta(itemMeta -> {
            List<Component> lore = itemMeta.lore();
            if (lore == null) lore = new ArrayList<>();

            lore.add(Component.empty());
            lore.add(noItalic(Component.text().append(
                    Component.text("가격: ", NamedTextColor.WHITE),
                    Component.text(String.format("%,d", cost) + BackasSurvivalPackExtended.MONEY_UNIT, NamedTextColor.YELLOW),
                    Component.text(" 또는 ", NamedTextColor.GRAY),
                    Component.text(String.format("%,d", cost) + PointManager.POINT_UNIT, NamedTextColor.AQUA)
            ).build()));
            lore.add(Component.empty());
            lore.add(noItalic(Component.text("[좌클릭]", NamedTextColor.GREEN).append(
                    Component.text(" 1개 구매 (돈)", NamedTextColor.WHITE))));
            lore.add(noItalic(Component.text("[쉬프트 + 좌클릭]", NamedTextColor.GREEN).append(
                    Component.text(" 64개 구매 (돈)", NamedTextColor.WHITE))));
            lore.add(noItalic(Component.text("[우클릭]", NamedTextColor.AQUA).append(
                    Component.text(" 1개 구매 (잠수 포인트)", NamedTextColor.WHITE))));
            lore.add(noItalic(Component.text("[쉬프트 + 우클릭]", NamedTextColor.AQUA).append(
                    Component.text(" 64개 구매 (잠수 포인트)", NamedTextColor.WHITE))));

            itemMeta.lore(lore);
        });
        return cloned;
    }

    private static ItemStack createTitleItem(Title title, UserDataPoint data) {
        boolean owned = data.hasTitle(title.name());
        boolean equipped = title.name().equals(data.getEquippedTitle());

        ItemStack item = new ItemStack(Material.NAME_TAG);
        item.editMeta(meta -> {
            meta.displayName(title.display().decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(noItalic(Component.text("칭호", NamedTextColor.GRAY)));
            lore.add(Component.empty());
            if (equipped) {
                lore.add(noItalic(Component.text("✔ 장착중", NamedTextColor.GREEN)));
                lore.add(Component.empty());
                lore.add(noItalic(Component.text("[좌클릭]", NamedTextColor.GREEN)
                        .append(Component.text(" 장착 해제", NamedTextColor.WHITE))));
            } else if (owned) {
                lore.add(noItalic(Component.text("보유중", NamedTextColor.AQUA)));
                lore.add(Component.empty());
                lore.add(noItalic(Component.text("[좌클릭]", NamedTextColor.GREEN)
                        .append(Component.text(" 장착", NamedTextColor.WHITE))));
            } else {
                lore.add(noItalic(Component.text("가격: ", NamedTextColor.WHITE)
                        .append(Component.text(String.format("%,d", title.getCost()) + PointManager.POINT_UNIT, NamedTextColor.AQUA))));
                lore.add(Component.empty());
                lore.add(noItalic(Component.text("[좌클릭]", NamedTextColor.GREEN)
                        .append(Component.text(" 구매 후 장착 (잠수 포인트)", NamedTextColor.WHITE))));
            }
            meta.lore(lore);

            if (equipped) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        });
        return item;
    }

    private static ItemStack createInfoItem(Player player, UserDataPoint pointData) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        item.editMeta(meta -> {
            meta.displayName(noItalic(Component.text("내 지갑", NamedTextColor.GOLD)));
            meta.lore(List.of(
                    noItalic(Component.text("돈: ", NamedTextColor.WHITE)
                            .append(Component.text(String.format("%,d", MoneyManager.getMoney(player)) + BackasSurvivalPackExtended.MONEY_UNIT, NamedTextColor.YELLOW))),
                    noItalic(Component.text("잠수 포인트: ", NamedTextColor.WHITE)
                            .append(Component.text(String.format("%,d", pointData.getAmount()) + PointManager.POINT_UNIT, NamedTextColor.AQUA))),
                    Component.empty(),
                    noItalic(Component.text("접속 1분마다 50~100포인트,", NamedTextColor.GRAY)),
                    noItalic(Component.text("2명 이상 접속 중이면 2배!", NamedTextColor.GRAY))
            ));
        });
        return item;
    }

    private static ItemStack createUnequipItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        item.editMeta(meta -> {
            meta.displayName(noItalic(Component.text("칭호 해제", NamedTextColor.RED)));
            meta.lore(List.of(
                    noItalic(Component.text("[좌클릭]", NamedTextColor.GREEN)
                            .append(Component.text(" 장착중인 칭호를 해제합니다.", NamedTextColor.WHITE)))
            ));
        });
        return item;
    }

    private static Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }

    public static int getCost(int slot, int page) {
        ItemStack item = getItem(slot, page);
        if (item == null) return 0;
        return LIST_OF_ITEMS.getOrDefault(item, 0);
    }

    public static ItemStack getItem(int slot, int page) {
        if (slot < 0 || slot >= ITEM_AREA_SIZE) return null;
        int index = (page - 1) * ITEM_AREA_SIZE + slot;
        List<ItemStack> items = new ArrayList<>(LIST_OF_ITEMS.keySet());
        if (index < 0 || items.size() <= index) return null;
        return items.get(index);
    }

    public static Title getTitle(int slot) {
        int index = slot - TITLE_ROW_START;
        Title[] titles = Title.values();
        if (index < 0 || index >= titles.length) return null;
        return titles[index];
    }
}
