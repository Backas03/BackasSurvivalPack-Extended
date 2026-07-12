package kr.kro.backas.backassurvivalpackextended.command.admin;

import kr.kro.backas.backassurvivalpackextended.point.PointManager;
import net.kyori.adventure.text.Component;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PointAdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("명령어 사용법: /" + label + " [give|take|set] [이름] [수량]"));
            return false;
        }
        Player target = Bukkit.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("해당 플레이어를 찾을 수 없습니다.", Palette.RED));
            return false;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("수량은 숫자여야 합니다.", Palette.RED));
            return false;
        }
        if (args[0].equalsIgnoreCase("give")) {
            PointManager.addPoint(target, amount);
            sender.sendMessage(Component.text("성공적으로 " + target.getName() + "님에게 " + amount + PointManager.POINT_UNIT + "를 지급하였습니다.", Palette.GREEN));
            return true;
        }
        if (args[0].equalsIgnoreCase("take")) {
            PointManager.removePoint(target, amount);
            sender.sendMessage(Component.text("성공적으로 " + target.getName() + "님의 " + amount + PointManager.POINT_UNIT + "를 차감하였습니다.", Palette.GREEN));
            return true;
        }
        if (args[0].equalsIgnoreCase("set")) {
            PointManager.setPoint(target, amount);
            sender.sendMessage(Component.text("성공적으로 " + target.getName() + "님의 잠수 포인트를 " + amount + PointManager.POINT_UNIT + "로 설정하였습니다.", Palette.GREEN));
            return true;
        }
        sender.sendMessage(Component.text("명령어 사용법: /" + label + " [give|take|set] [이름] [수량]"));
        return false;
    }
}
