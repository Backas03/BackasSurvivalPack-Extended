package kr.kro.backas.backassurvivalpackextended.util;

import net.kyori.adventure.text.format.TextColor;

/**
 * 플러그인 공용 파스텔톤 색상 팔레트.
 * 채팅/GUI 메시지 색상은 NamedTextColor 대신 이 팔레트를 사용한다.
 */
public final class Palette {

    public static final TextColor WHITE = TextColor.color(0xF7F7FA);
    public static final TextColor GRAY = TextColor.color(0xC9CAD4);
    public static final TextColor DARK_GRAY = TextColor.color(0xA7A9B4);
    public static final TextColor RED = TextColor.color(0xFFB3B3);
    public static final TextColor GREEN = TextColor.color(0xB5EAB8);
    public static final TextColor YELLOW = TextColor.color(0xFFF3A3);
    public static final TextColor GOLD = TextColor.color(0xFFD8A8);
    public static final TextColor AQUA = TextColor.color(0xA0E7E5);
    public static final TextColor BLUE = TextColor.color(0xAEC6FF);
    public static final TextColor PURPLE = TextColor.color(0xCBAAE8);
    public static final TextColor PINK = TextColor.color(0xFFB5E8);

    private Palette() {
    }
}
