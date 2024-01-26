package kr.kro.backas.backassurvivalpackextended.teleport.command;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.teleport.TeleportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TPAcceptCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player to)) {
            return false;
        }
        TeleportManager teleportManager = BackasSurvivalPackExtended.getTeleportManager();
        if (strings.length == 0) {
            to.sendMessage(Component.text("사용법: ", NamedTextColor.RED).append(
                    Component.text(" /tpaccept [이름]", NamedTextColor.GREEN)
            ));
            return false;
        }
        String name = strings[0];
        Player from = Bukkit.getPlayer(name);
        if (from == null) {
            to.sendMessage(Component.text("해당 플레이어를 찾을 수 없습니다.", NamedTextColor.RED));
            return false;
        }
        teleportManager.tryResponse(to, from, true);
        return false;
    }
}
