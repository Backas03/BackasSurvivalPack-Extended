package kr.kro.backas.backassurvivalpackextended.point.title;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

public enum Title {
    NEWBIE("뉴비", TextColor.color(0xB5EAB8), 20000, false),
    HOMEWORK("숙제하는중", TextColor.color(0xFFF3A3), 80000, false),
    SALARY_THIEF("월급루팡", TextColor.color(0xFFD8A8), 120000, false),
    DIVER("잠수부", TextColor.color(0xA0E7E5), 200000, false),
    FISHING_GHOST("낚시터지박령", TextColor.color(0x86CECB), 300000, false),
    SERVER_KEEPER("서버지킴이", TextColor.color(0xEAEAFF), 450000, false),
    AFK_KING("잠수왕", TextColor.color(0xAEC6FF), 750000, true),
    SEMI_WILD_ADDICT("반야생폐인", TextColor.color(0xCBAAE8), 1500000, true),
    LIVING_FOSSIL("살아있는화석", TextColor.color(0xFFB5E8), 2500000, true);

    private final String displayName;
    private final TextColor color;
    private final int cost;
    private final boolean bold;

    Title(String displayName, TextColor color, int cost, boolean bold) {
        this.displayName = displayName;
        this.color = color;
        this.cost = cost;
        this.bold = bold;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCost() {
        return cost;
    }

    public Component display() {
        Component component = Component.text("[" + displayName + "]", color);
        if (bold) {
            component = component.decorate(TextDecoration.BOLD);
        }
        return component;
    }

    @Nullable
    public static Title byId(@Nullable String id) {
        if (id == null) return null;
        for (Title title : values()) {
            if (title.name().equals(id)) {
                return title;
            }
        }
        return null;
    }
}
