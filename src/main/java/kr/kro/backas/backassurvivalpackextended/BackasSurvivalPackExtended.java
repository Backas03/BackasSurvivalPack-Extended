package kr.kro.backas.backassurvivalpackextended;

import kr.kro.backas.backassurvivalpackextended.command.*;
import kr.kro.backas.backassurvivalpackextended.command.admin.ItemAdminCommand;
import kr.kro.backas.backassurvivalpackextended.command.admin.MoneyAdminCommand;
import kr.kro.backas.backassurvivalpackextended.easyshop.EasyShopListener;
import kr.kro.backas.backassurvivalpackextended.easyshop.ItemListener;
import kr.kro.backas.backassurvivalpackextended.ranking.RankingManager;
import kr.kro.backas.backassurvivalpackextended.teleport.TeleportManager;
import kr.kro.backas.backassurvivalpackextended.teleport.command.TPACommand;
import kr.kro.backas.backassurvivalpackextended.teleport.command.TPADenyCommand;
import kr.kro.backas.backassurvivalpackextended.teleport.command.TPAHereCommand;
import kr.kro.backas.backassurvivalpackextended.teleport.command.TPAcceptCommand;
import kr.kro.backas.backassurvivalpackextended.user.UserListener;
import kr.kro.backas.backassurvivalpackextended.user.UserManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class BackasSurvivalPackExtended extends JavaPlugin {

    private static BackasSurvivalPackExtended instance;
    private static UserManager userManager;
    private static RankingManager rankingManager;
    private static TeleportManager teleportManager;

    public static BackasSurvivalPackExtended getInstance() {
        return instance;
    }

    public static UserManager getUserManager() {
        return userManager;
    }

    public static RankingManager getRankingManager() {
        return rankingManager;
    }

    public static TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public static String MONEY_UNIT = " 굥";


    public BackasSurvivalPackExtended() {
        instance = this;
        teleportManager = new TeleportManager();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        userManager = new UserManager();
        rankingManager = new RankingManager();

        getServer().getPluginManager().registerEvents(new UserListener(), this);
        getServer().getPluginManager().registerEvents(new EasyShopListener(), this);
        getServer().getPluginManager().registerEvents(new ItemListener(), this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        getCommand("ranking").setExecutor(new RankingCommand());
        getCommand("money").setExecutor(new MoneyCommand());
        getCommand("money-admin").setExecutor(new MoneyAdminCommand());
        getCommand("sell").setExecutor(new EasyShopCommand());
        getCommand("shop").setExecutor(new EasyPurchaseCommand());
        getCommand("item-admin").setExecutor(new ItemAdminCommand());
        getCommand("shop-list").setExecutor(new ShopListCommand());

        getCommand("tpa").setExecutor(new TPACommand());
        getCommand("tpaccept").setExecutor(new TPAcceptCommand());
        getCommand("tpadeny").setExecutor(new TPADenyCommand());
        getCommand("tpahere").setExecutor(new TPAHereCommand());

        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.initUser(player);
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Block chestBlock1 = Bukkit.getWorld("world_the_end").getBlockAt(303, 0, -1);
            if (chestBlock1.getState() instanceof Chest chest1) {
                chest1.getBlockInventory().clear();
            }
        }, 0, 20 * 5);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        userManager.saveAll();
    }
}
