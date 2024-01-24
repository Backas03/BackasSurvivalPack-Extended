package kr.kro.backas.backassurvivalpackextended.command;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.MoneyManager;
import kr.kro.backas.backassurvivalpackextended.api.PlayerSendMoneyEvent;
import kr.kro.backas.backassurvivalpackextended.user.UserManager;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoney;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoneyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text().append(
                            Component.text("[정보] ", NamedTextColor.GREEN),
                            Component.text("현재 보유 금액: ", NamedTextColor.WHITE),
                            Component.text(MoneyManager.getMoney(player) + "원", NamedTextColor.YELLOW)
                    )
            );
            player.sendMessage(Component.text().append(
                    Component.text("[도움말] ", NamedTextColor.GREEN),
                    Component.text("/" + label + " 보내기 [이름] [금액]"),
                    Component.text(" - 플레이어에게 돈을 송금합니다. (플레이어가 오프라인이면 송금 불가)", NamedTextColor.GRAY)
            ));
            return false;
        }
        if (args[0].equals("보내기")) {
            if (args.length < 3) {
                player.sendMessage(Component.text("명령어 사용법: /" + command.getName() + " 보내기 [이름] [금액]", NamedTextColor.RED));
                return false;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(Component.text("해당 플레이어를 찾을 수 없습니다.", NamedTextColor.RED));
                return false;
            }
            if (target.equals(player)) {
                player.sendMessage(Component.text("자기 자신에게는 송금할 수 없습니다.", NamedTextColor.RED));
                return false;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) {
                    player.sendMessage(Component.text("금액은 1 이상이어야 합니다.", NamedTextColor.RED));
                    return false;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("금액은 숫자여야 합니다.", NamedTextColor.RED));
                return false;
            }
            if (MoneyManager.getMoney(player) < amount) {
                player.sendMessage(Component.text("잔액이 부족합니다.", NamedTextColor.RED));
                return false;
            }
            MoneyManager.sendMoney(player, target, amount);
            player.sendMessage(Component.text().append(
                    Component.text(target.getName(), NamedTextColor.GREEN),
                    Component.text(" 님께 ", NamedTextColor.WHITE),
                    Component.text(amount + "원", NamedTextColor.YELLOW),
                    Component.text("을 송금하였습니다.", NamedTextColor.WHITE)
            ));
            target.sendMessage(Component.text().append(
                    Component.text(player.getName(), NamedTextColor.GREEN),
                    Component.text(" 님께서 ", NamedTextColor.WHITE),
                    Component.text(amount + "원", NamedTextColor.YELLOW),
                    Component.text("을 송금하였습니다.", NamedTextColor.WHITE)
            ));
        }
        return false;
    }
}
