package kr.kro.backas.backassurvivalpackextended.easyshop;

import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ItemFactory {

    /** 자동심기 권한부여서: 사용 시 계정에 영구 적용 */
    public static ItemStack getAutoReplantLicense() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        item.editMeta(itemMeta -> {
            itemMeta.displayName(Component.text("자동심기 권한부여서", Palette.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("다 자란 작물을 수확하면 씨앗 1개를 소모해", Palette.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("같은 자리에 자동으로 다시 심습니다.", Palette.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text().append(
                    Component.text("[우클릭] ", Palette.GREEN),
                    Component.text("사용 시 계정에 영구 적용", Palette.WHITE)
            ).build().decoration(TextDecoration.ITALIC, false));
            itemMeta.lore(lore);
        });
        return item;
    }

    /** 자동줍기 권한부여서: 사용 시 계정에 영구 적용 */
    public static ItemStack getAutoPickupLicense() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        item.editMeta(itemMeta -> {
            itemMeta.displayName(Component.text("자동줍기 권한부여서", Palette.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("작물/광물 드랍이 바닥에 떨어지지 않고", Palette.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("인벤토리로 바로 들어옵니다.", Palette.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text().append(
                    Component.text("[우클릭] ", Palette.GREEN),
                    Component.text("사용 시 계정에 영구 적용", Palette.WHITE)
            ).build().decoration(TextDecoration.ITALIC, false));
            itemMeta.lore(lore);
        });
        return item;
    }
    public static ItemStack getHead() {
        ItemStack item = new ItemStack(Material.IRON_HOE);
        item.editMeta(itemMeta -> {
            itemMeta.displayName(Component.text("머리 추수권", NamedTextColor.GRAY));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text().append(
                    Component.text("플레이어 우클릭: ", NamedTextColor.WHITE),
                    Component.text(" 우클릭한 플레이어의 머리를 얻습니다", NamedTextColor.YELLOW)
                    ).build().decoration(TextDecoration.ITALIC, false)
            );
            lore.add(Component.text().append(
                    Component.text("쉬프트 우클릭: ", NamedTextColor.WHITE),
                    Component.text(" 자신의 머리를 얻습니다", NamedTextColor.GOLD)
            ).build().decoration(TextDecoration.ITALIC, false));
            itemMeta.lore(lore);
        });
        return item;
    }

    public static ItemStack getTPCoolTimeClear() {
        ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(itemMeta -> {
            itemMeta.displayName(Component.text("텔레포트 쿨타임 초기화권", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text().append(
                            Component.text("클릭하여 텔레포트 쿨타임을 초기화합니다", NamedTextColor.GRAY)
                    ).build().decoration(TextDecoration.ITALIC, false)
            );
            itemMeta.lore(lore);
        });
        return item;
    }
}
