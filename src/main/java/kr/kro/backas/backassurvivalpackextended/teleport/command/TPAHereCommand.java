package kr.kro.backas.backassurvivalpackextended.teleport.command;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.teleport.TeleportManager;
import kr.kro.backas.backassurvivalpackextended.teleport.TeleportType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class TPAHereCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        TeleportManager teleportManager = BackasSurvivalPackExtended.getTeleportManager();
        if (strings.length == 0) {
            player.sendMessage(Component.text().append(
                    Component.text("[사용법]", NamedTextColor.GREEN),
                    Component.text(" /tpahere [이름] ", NamedTextColor.WHITE),
                    Component.text(" - 유저에게 자신의 위치로 텔레포트 요청을 보냅니다.", NamedTextColor.GRAY)
            ));

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
        teleportManager.tryRequest(player, to, TeleportType.HERE);
        return false;
    }
}
