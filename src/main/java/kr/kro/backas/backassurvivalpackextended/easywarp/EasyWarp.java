package kr.kro.backas.backassurvivalpackextended.easywarp;

import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EasyWarp {
    public static final LinkedHashMap<String, Node> WARPS = new LinkedHashMap<>();
    public static final Component INVENTORY_TITLE = Component.text("워프 목록")
            .decorate(TextDecoration.BOLD);
    public static final int COST = 0;

    static {
        WARPS.put("spawn", new Node(
                Component.text("스폰", NamedTextColor.GREEN),
                new ItemStack(Material.GRASS_BLOCK),
                new Location(Bukkit.getWorld("world"), 0, 70, -0, 90, 0))
        );

        WARPS.put("town", new Node(
                Component.text("마을", NamedTextColor.GREEN),
                new ItemStack(Material.CAMPFIRE),
                new Location(Bukkit.getWorld("world"), 39, 64, -1029, 0, 0))
        );
        WARPS.put("town-2", new Node(
                Component.text("마을 2", NamedTextColor.GREEN),
                new ItemStack(Material.CAMPFIRE, 2),
                new Location(Bukkit.getWorld("world"), -317.347, 68, -1044.584, -172.3f, 11.4f))
        );
        WARPS.put("end-portal", new Node(
                Component.text("엔더 포탈", Palette.PURPLE),
                new ItemStack(Material.END_PORTAL_FRAME),
                new Location(Bukkit.getWorld("world"), -839.5, 27, -2452.5, 180, 0))
        );
        WARPS.put("xp-farm", new Node(
                Component.text("경험치 공장", Palette.AQUA),
                new ItemStack(Material.EXPERIENCE_BOTTLE),
                new Location(Bukkit.getWorld("world_the_end"), 239.5, 57, 4.5, -90, 0))
        );
        /*WARPS.put("ender-farm", new Node(
                Component.text("엔더팜", NamedTextColor.AQUA),
                new ItemStack(Material.EXPERIENCE_BOTTLE),
                new Location(Bukkit.getWorld("world_the_end"), 250, 49, 0.5, -90 ,0))
        );
        WARPS.put("nether", new Node(
                Component.text("네더 유적", NamedTextColor.GOLD),
                new ItemStack(Material.BLAZE_ROD),
                new Location(Bukkit.getWorld("world_nether"), 254, 73, -31, 0, 0))
        );
        WARPS.put("creeper", new Node(
                Component.text("화약 공장 정상", NamedTextColor.GREEN),
                new ItemStack(Material.CREEPER_HEAD),
                new Location(Bukkit.getWorld("world"), -408, 180, -2439))
        );
        WARPS.put("gunpowder", new Node(
                Component.text("화약 공장 창고", NamedTextColor.DARK_GRAY),
                new ItemStack(Material.GUNPOWDER),
                new Location(Bukkit.getWorld("world"), -400, 63, -2439, 90, 0))
        );
        WARPS.put("warden", new Node(
                Component.text("고대 도시", NamedTextColor.GRAY),
                new ItemStack(Material.SCULK_CATALYST),
                new Location(Bukkit.getWorld("world"), 5425, -45, 127))
        );

         */
    }

    public static final class Node {
        private final Component name;
        private final ItemStack icon;
        private final Location location;

        public Node(Component name, ItemStack icon, Location location) {
            this.name = name;
            this.icon = icon;
            this.icon.editMeta(meta -> {
                meta.displayName(name.decoration(TextDecoration.ITALIC, false));
                List<Component> lore = meta.lore();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add(Component.text().append(
                        Component.text("워프 시 ", NamedTextColor.WHITE),
                        Component.text(COST, NamedTextColor.GREEN),
                        Component.text("원이 ", NamedTextColor.WHITE),
                        Component.text("소모 ", NamedTextColor.RED),
                        Component.text("됩니다.", NamedTextColor.WHITE)).build()
                        .decoration(TextDecoration.ITALIC, false)
                );
                lore.add(Component.empty());
                lore.add(Component.text().append(
                        Component.text("월드: ", NamedTextColor.GRAY),
                        Component.text(convertWorld(location.getWorld()), NamedTextColor.GOLD))
                        .build()
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text().append(
                        Component.text("X: ", NamedTextColor.GRAY),
                        Component.text(location.getBlockX(), NamedTextColor.YELLOW)
                        ).build()
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text().append(
                                Component.text("Y: ", NamedTextColor.GRAY),
                                Component.text(location.blockY(), NamedTextColor.YELLOW)
                        ).build()
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text().append(
                                Component.text("Z: ", NamedTextColor.GRAY),
                                Component.text(location.getBlockZ(), NamedTextColor.YELLOW)
                        ).build()
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            this.location = location;
        }

        private String convertWorld(World world) {
            return switch (world.getName()) {
                case "world" -> "오버월드";
                case "world_the_end" -> "엔드";
                case "world_nether" -> "네더";
                default -> world.getName();
            };
        }

        public @NotNull CompletableFuture<Boolean> teleport(Player player) {
            return player.teleportAsync(location);
        }
    }

    public static Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, 27, INVENTORY_TITLE);
        WARPS.forEach((key, value) -> inventory.addItem(value.icon));
        return inventory;
    }

    public static Node getNode(int slot) {
        return new ArrayList<>(WARPS.values()).get(slot);
    }
}
