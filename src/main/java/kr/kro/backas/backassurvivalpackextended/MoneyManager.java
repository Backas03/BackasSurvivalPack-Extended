package kr.kro.backas.backassurvivalpackextended;

import kr.kro.backas.backassurvivalpackextended.api.PlayerMoneyUpdateEvent;
import kr.kro.backas.backassurvivalpackextended.api.PlayerSendMoneyEvent;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoney;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoneyUse;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MoneyManager {
    public static int getMoney(Player player) {
        return getUserDataMoney(player).getAmount();
    }

    public static void setMoney(Player player, int amount) {
        UserDataMoney money = getUserDataMoney(player);
        int oldAmount = money.getAmount();
        Bukkit.getPluginManager().callEvent(new PlayerMoneyUpdateEvent(player, oldAmount, money.setAmount(amount).getAmount()));
    }

    public static void addMoney(Player player, int amount) {
        UserDataMoney money = getUserDataMoney(player);
        int oldAmount = money.getAmount();
        Bukkit.getPluginManager().callEvent(new PlayerMoneyUpdateEvent(player, oldAmount, money.add(amount).getAmount()));
    }

    public static void removeMoney(Player player, int amount, boolean logForMoneyUse) {
        UserDataMoney money = getUserDataMoney(player);
        int oldAmount = money.getAmount();
        money.subtract(amount);
        Bukkit.getPluginManager().callEvent(new PlayerMoneyUpdateEvent(player, oldAmount, money.getAmount()));
        if (logForMoneyUse) {
            BackasSurvivalPackExtended.getUserManager()
                    .getUser(player)
                    .getDataContainer()
                    .getOrLoadAsync(UserDataMoneyUse.class)
                    .thenAccept(data -> data.add(amount));
        }
    }

    public static void sendMoney(Player from, Player to, int amount) {
        removeMoney(from, amount, false);
        addMoney(to, amount);
        Bukkit.getPluginManager().callEvent(new PlayerSendMoneyEvent(from, to, amount));
    }

    private static UserDataMoney getUserDataMoney(Player player) {
        return BackasSurvivalPackExtended.getUserManager()
                .getUser(player)
                .getDataContainer()
                .getOrLoad(UserDataMoney.class);
    }
}
