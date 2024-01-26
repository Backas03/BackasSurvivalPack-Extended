package kr.kro.backas.backassurvivalpackextended.easyshop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Item {
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
