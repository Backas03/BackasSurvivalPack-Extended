package kr.kro.backas.backassurvivalpackextended.teleport.command;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TPADenyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player to)) {
            return false;
        }
        if (strings.length == 0) {
            to.sendMessage(Component.text("사용법: ", NamedTextColor.RED).append(
                    Component.text(" /tpadeny [이름]", NamedTextColor.GREEN)
            ));
            return false;
        }
        String name = strings[0];
        Player from = Bukkit.getPlayer(name);
        if (from == null) {
            to.sendMessage(Component.text("해당 플레이어를 찾을 수 없습니다.", NamedTextColor.RED));
            return false;
        }
        BackasSurvivalPackExtended.getTeleportManager()
                .tryResponse(to, from, false);
        return false;
    }
}
