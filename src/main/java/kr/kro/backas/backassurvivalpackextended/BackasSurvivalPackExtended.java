package kr.kro.backas.backassurvivalpackextended;

import kr.kro.backas.backassurvivalpackextended.coin.CoinManager;
import kr.kro.backas.backassurvivalpackextended.command.*;
import kr.kro.backas.backassurvivalpackextended.command.admin.ItemAdminCommand;
import kr.kro.backas.backassurvivalpackextended.command.admin.MoneyAdminCommand;
import kr.kro.backas.backassurvivalpackextended.command.admin.PointAdminCommand;
import kr.kro.backas.backassurvivalpackextended.easyshop.EasyShopListener;
import kr.kro.backas.backassurvivalpackextended.easyshop.ItemListener;
import kr.kro.backas.backassurvivalpackextended.easyshop.config.EasyPurchaseConfig;
import kr.kro.backas.backassurvivalpackextended.easywarp.EasyWarpListener;
import kr.kro.backas.backassurvivalpackextended.farming.FarmingListener;
import kr.kro.backas.backassurvivalpackextended.menu.MenuListener;
import kr.kro.backas.backassurvivalpackextended.mining.MiningListener;
import kr.kro.backas.backassurvivalpackextended.point.PointManager;
import kr.kro.backas.backassurvivalpackextended.point.title.TitleListener;
import kr.kro.backas.backassurvivalpackextended.ranking.RankingManager;
import kr.kro.backas.backassurvivalpackextended.teleport.TeleportManager;
import kr.kro.backas.backassurvivalpackextended.teleport.command.TPACommand;
import kr.kro.backas.backassurvivalpackextended.teleport.command.TPADenyCommand;
import kr.kro.backas.backassurvivalpackextended.teleport.command.TPAHereCommand;
import kr.kro.backas.backassurvivalpackextended.teleport.command.TPAcceptCommand;
import kr.kro.backas.backassurvivalpackextended.user.UserListener;
import kr.kro.backas.backassurvivalpackextended.user.UserManager;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataCoin;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataFarming;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMining;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoney;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoneyUse;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataPoint;
import kr.kro.backas.backassurvivalpackextended.util.PlacedBlockTracker;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class BackasSurvivalPackExtended extends JavaPlugin {

    private static BackasSurvivalPackExtended instance;
    private static UserManager userManager;
    private static RankingManager rankingManager;
    private static TeleportManager teleportManager;
    private static CoinManager coinTicker;
    private static PointManager pointManager;

    public static BackasSurvivalPackExtended getInstance() {
        return instance;
    }

    public static UserManager getUserManager() {
        return userManager;
    }

    public static RankingManager getRankingManager() {
        return rankingManager;
    }

    public static CoinManager getCoinTicker() {
        return coinTicker;
    }

    public static TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public static PointManager getPointManager() {
        return pointManager;
    }

    public static String MONEY_UNIT = " 원";


    public BackasSurvivalPackExtended() {
        instance = this;
        teleportManager = new TeleportManager();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        userManager = new UserManager();
        rankingManager = new RankingManager();
        coinTicker = new CoinManager();
        coinTicker.start();
        pointManager = new PointManager();
        pointManager.start();

        getServer().getPluginManager().registerEvents(new UserListener(), this);
        getServer().getPluginManager().registerEvents(new EasyShopListener(), this);
        getServer().getPluginManager().registerEvents(new ItemListener(), this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getPluginManager().registerEvents(new EasyWarpListener(), this);
        getServer().getPluginManager().registerEvents(new TitleListener(), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(), this);
        getServer().getPluginManager().registerEvents(new MiningListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

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

        getCommand("warp").setExecutor(new EasyWarpCommand());

        getCommand("coin").setExecutor(new CoinCommand());

        getCommand("point").setExecutor(new PointCommand());
        getCommand("point-admin").setExecutor(new PointAdminCommand());
        getCommand("titleshop").setExecutor(new TitleCommand());
        getCommand("farming").setExecutor(new FarmingCommand());
        getCommand("mining").setExecutor(new MiningCommand());

        ConfigurationSerialization.registerClass(UserDataMoney.class);
        ConfigurationSerialization.registerClass(UserDataMoneyUse.class);
        ConfigurationSerialization.registerClass(UserDataPoint.class);
        ConfigurationSerialization.registerClass(UserDataFarming.class);
        ConfigurationSerialization.registerClass(UserDataMining.class);

        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.initUser(player);
        }

        EasyPurchaseConfig.load();
        PlacedBlockTracker.load();

        /* 엔더월드 경험치팜 상자 자동으로 비우는 놈
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Block chestBlock1 = Bukkit.getWorld("world_the_end").getBlockAt(254, 48, 0);
            if (chestBlock1.getState() instanceof Chest chest1) {
                chest1.getBlockInventory().clear();
            }
        }, 0, 20 * 5);
         */

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        userManager.saveAll();
        PlacedBlockTracker.save();
    }
}
