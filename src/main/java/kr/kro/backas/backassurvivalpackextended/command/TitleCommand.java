package kr.kro.backas.backassurvivalpackextended.command;

import kr.kro.backas.backassurvivalpackextended.easyshop.purchase.EasyPurchaseInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TitleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        // 칭호는 통합 상점 GUI 맨 아랫줄에 있다.
        EasyPurchaseInventory.open(player);
        return true;
    }
}
