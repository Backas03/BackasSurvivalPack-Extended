package kr.kro.backas.backassurvivalpackextended.command.admin;

import kr.kro.backas.backassurvivalpackextended.MoneyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoneyAdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("/" + label + " [give|take|set] [이름] [금액]"));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("명령어 사용법: /" + label + " [give|take|set] [이름] [금액]"));
            return false;
        }
        Player target = Bukkit.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("해당 플레이어를 찾을 수 없습니다.", NamedTextColor.RED));
            return false;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("금액은 숫자여야 합니다.", NamedTextColor.RED));
            return false;
        }
        if (args[0].equalsIgnoreCase("give")) {
            MoneyManager.addMoney(target, amount);
            sender.sendMessage(Component.text("성공적으로 " + target.getName() + "님에게 " + amount + "원을 지급하였습니다.", NamedTextColor.GREEN));
            target.sendMessage(Component.text("관리자로부터 " + amount + "원을 지급받았습니다.", NamedTextColor.GREEN));
            return true;
        }
        if (args[0].equalsIgnoreCase("take")) {
            MoneyManager.removeMoney(target, amount);
            sender.sendMessage(Component.text("성공적으로 " + target.getName() + "님의 " + amount + "원을 차감하였습니다.", NamedTextColor.GREEN));
            target.sendMessage(Component.text("관리자가 당신의 돈을 " + amount + "원 차감하였습니다.", NamedTextColor.GREEN));
            return true;
        }
        if (args[0].equalsIgnoreCase("set")) {
            MoneyManager.setMoney(target, amount);
            sender.sendMessage(Component.text("성공적으로 " + target.getName() + "님의 돈을 " + amount + "원으로 설정하였습니다.", NamedTextColor.GREEN));
            target.sendMessage(Component.text("관리자가 당신의 돈을 " + amount + "원으로 설정하였습니다.", NamedTextColor.GREEN));
            return true;
        }
        return false;
    }
}
