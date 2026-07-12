package kr.kro.backas.backassurvivalpackextended.command;

import kr.kro.backas.backassurvivalpackextended.point.PointManager;
import net.kyori.adventure.text.Component;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PointCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        player.sendMessage(Component.text().append(
                Component.text("[정보] ", Palette.AQUA),
                Component.text("보유 잠수 포인트: ", Palette.WHITE),
                Component.text(String.format("%,d", PointManager.getPoint(player)) + PointManager.POINT_UNIT, Palette.YELLOW)
        ));
        player.sendMessage(Component.text().append(
                Component.text("[도움말] ", Palette.AQUA),
                Component.text("접속 중이면 1분마다 5~10포인트, 2명 이상 접속 중이면 2배로 적립됩니다.", Palette.GRAY)
        ));
        player.sendMessage(Component.text().append(
                Component.text("[도움말] ", Palette.AQUA),
                Component.text("/상점", Palette.WHITE),
                Component.text(" - 아이템을 포인트로도 구매할 수 있고, 맨 아랫줄에서 칭호를 구매/장착합니다.", Palette.GRAY)
        ));
        return true;
    }
}
