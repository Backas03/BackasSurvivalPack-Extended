package kr.kro.backas.backassurvivalpackextended.command.admin;

import kr.kro.backas.backassurvivalpackextended.easyshop.ItemFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ItemAdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return true;
        }
        if (strings.length == 0) {
            return true;
        }
        if (strings[0].equalsIgnoreCase("head")) {
            player.getInventory().addItem(ItemFactory.getHead());
            return true;
        }
        if (strings[0].equalsIgnoreCase("tp")) {
            player.getInventory().addItem(ItemFactory.getTPCoolTimeClear());
            return true;
        }
        return false;
    }
}
