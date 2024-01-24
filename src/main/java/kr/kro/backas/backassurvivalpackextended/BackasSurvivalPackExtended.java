package kr.kro.backas.backassurvivalpackextended;

import kr.kro.backas.backassurvivalpackextended.command.MoneyCommand;
import kr.kro.backas.backassurvivalpackextended.command.RankingCommand;
import kr.kro.backas.backassurvivalpackextended.command.admin.MoneyAdminCommand;
import kr.kro.backas.backassurvivalpackextended.ranking.RankingManager;
import kr.kro.backas.backassurvivalpackextended.user.UserListener;
import kr.kro.backas.backassurvivalpackextended.user.UserManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BackasSurvivalPackExtended extends JavaPlugin {

    private static BackasSurvivalPackExtended instance;
    private static UserManager userManager;
    private static RankingManager rankingManager;

    public static BackasSurvivalPackExtended getInstance() {
        return instance;
    }

    public static UserManager getUserManager() {
        return userManager;
    }

    public static RankingManager getRankingManager() {
        return rankingManager;
    }


    public BackasSurvivalPackExtended() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        userManager = new UserManager();
        rankingManager = new RankingManager();

        getServer().getPluginManager().registerEvents(new UserListener(), this);

        getCommand("ranking").setExecutor(new RankingCommand());
        getCommand("money").setExecutor(new MoneyCommand());
        getCommand("money-admin").setExecutor(new MoneyAdminCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        userManager.saveAll();
    }
}
