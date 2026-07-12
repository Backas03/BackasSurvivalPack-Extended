package kr.kro.backas.backassurvivalpackextended.command;

import kr.kro.backas.backassurvivalpackextended.farming.FarmingManager;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataFarming;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FarmingCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        UserDataFarming data = FarmingManager.getUserData(player);
        int level = FarmingManager.getLevel(data.getXp());

        Component progress = level >= FarmingManager.MAX_LEVEL
                ? Component.text("MAX", NamedTextColor.GOLD)
                : Component.text(FarmingManager.getXpIntoLevel(data.getXp())
                        + "/" + FarmingManager.xpToNext(level), NamedTextColor.YELLOW);

        player.sendMessage(Component.text().append(
                Component.text("[정보] ", NamedTextColor.GREEN),
                Component.text("농사 레벨: ", NamedTextColor.WHITE),
                Component.text("Lv." + level, NamedTextColor.GOLD),
                Component.text(" (경험치 ", NamedTextColor.GRAY),
                progress,
                Component.text(")", NamedTextColor.GRAY)
        ));
        if (level > 0) {
            String extraAmountText = level >= FarmingManager.MAX_LEVEL ? "2~3개"
                    : level >= 5 ? "1~2개" : "1개";
            player.sendMessage(Component.text().append(
                    Component.text("[특전] ", NamedTextColor.GREEN),
                    Component.text("수확 시 ", NamedTextColor.WHITE),
                    Component.text((int) (FarmingManager.getExtraDropChance(level) * 100) + "%", NamedTextColor.GOLD),
                    Component.text(" 확률로 작물 ", NamedTextColor.WHITE),
                    Component.text(extraAmountText, NamedTextColor.GOLD),
                    Component.text(" 추가 드랍", NamedTextColor.WHITE)
            ));
        }
        player.sendMessage(Component.text().append(
                Component.text("[도움말] ", NamedTextColor.GREEN),
                Component.text("다 자란 작물을 직접 수확하면 경험치를 얻습니다. " +
                        "레벨이 오를수록 추가 드랍 확률이 3%씩 늘고, 5레벨부터 1~2개, 10레벨엔 30% 확률로 2~3개!", NamedTextColor.GRAY)
        ));
        return true;
    }
}
