package kr.kro.backas.backassurvivalpackextended.teleport.command;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.teleport.TeleportManager;
import kr.kro.backas.backassurvivalpackextended.teleport.TeleportType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class TPACommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        TeleportManager teleportManager = BackasSurvivalPackExtended.getTeleportManager();
        if (strings.length == 0) {
            player.sendMessage(Component.text().append(
                    Component.text("[사용법]", NamedTextColor.GREEN),
                    Component.text(" /tpa [이름] ", NamedTextColor.WHITE),
                    Component.text(" - 유저에게 텔레포트 요청을 보냅니다.", NamedTextColor.GRAY)
            ));

            Map<UUID, TeleportType> pendingTeleports = teleportManager.getPendingTeleports(player);
            if (!pendingTeleports.isEmpty()) {
                player.sendMessage(Component.text("=====요청받은 텔레포트 목록=====", NamedTextColor.GOLD));
                for (Map.Entry<UUID, TeleportType> entry : pendingTeleports.entrySet()) {
                    Player requester = Bukkit.getPlayer(entry.getKey());
                    if (requester == null) continue;
                    player.sendMessage(Component.text().append(
                            Component.text("    ", NamedTextColor.WHITE),
                            Component.text(requester.getName(), NamedTextColor.GRAY),
                            entry.getValue() == TeleportType.HERE ? Component.text("(자신의 위치로 이동 요청)", NamedTextColor.GRAY) : Component.empty(),
                            Component.space(),
                            Component.text("[수락]", NamedTextColor.GREEN)
                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/tpaccept " + requester.getName())))
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + requester.getName())),
                            Component.space(),
                            Component.text("[거절]", NamedTextColor.RED)
                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/tpadeny " + requester.getName())))
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny " + requester.getName()))
                    ));
                }
            }

            if (teleportManager.canRequest(player)) {
                player.sendMessage(Component.text().append(
                        Component.text("텔레포트 요청을 보낼 수 있습니다.", NamedTextColor.GREEN),
                        Component.text(" (텔레포트 요청은 15분의 쿨타임이 있습니다)", NamedTextColor.GRAY)
                ));
                return false;
            }
            Duration leftTime = teleportManager.getTeleportLeftTime(player);
            player.sendMessage(Component.text().append(
                    Component.text("텔레포트는 15분마다 요청을 보낼 수 있습니다.", NamedTextColor.RED),
                    Component.text(String.format(" (남은 대기시간: %d분 %d초)", leftTime.getSeconds() / 60, leftTime.getSeconds() % 60), NamedTextColor.GRAY)
            ));
            return false;
        }
        String name = strings[0];
        Player to = Bukkit.getPlayer(name);

        if (to == null) {
            player.sendMessage(Component.text("해당 플레이어를 찾을 수 없습니다.", NamedTextColor.RED));
            return false;
        }
        teleportManager.tryRequest(player, to, TeleportType.TO);
        return false;
    }
}
