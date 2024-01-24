package kr.kro.backas.backassurvivalpackextended.command;

import kr.kro.backas.backassurvivalpackextended.easyshop.EasyShopInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EasyShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        player.openInventory(EasyShopInventory.newInventory());
        return true;
    }
}
