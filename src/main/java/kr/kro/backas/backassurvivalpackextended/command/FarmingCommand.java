package kr.kro.backas.backassurvivalpackextended.command;

import kr.kro.backas.backassurvivalpackextended.farming.FarmingManager;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataFarming;
import net.kyori.adventure.text.Component;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
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
                ? Component.text("MAX", Palette.GOLD)
                : Component.text(FarmingManager.getXpIntoLevel(data.getXp())
                        + "/" + FarmingManager.xpToNext(level), Palette.YELLOW);

        player.sendMessage(Component.text().append(
                Component.text("[정보] ", Palette.GREEN),
                Component.text("농사 레벨: ", Palette.WHITE),
                Component.text("Lv." + level, Palette.GOLD),
                Component.text(" (경험치 ", Palette.GRAY),
                progress,
                Component.text(")", Palette.GRAY)
        ));
        if (level > 0) {
            String extraAmountText = level >= FarmingManager.MAX_LEVEL ? "2~3개"
                    : level >= 5 ? "1~2개" : "1개";
            player.sendMessage(Component.text().append(
                    Component.text("[특전] ", Palette.GREEN),
                    Component.text("수확 시 ", Palette.WHITE),
                    Component.text((int) (FarmingManager.getExtraDropChance(level) * 100) + "%", Palette.GOLD),
                    Component.text(" 확률로 작물 ", Palette.WHITE),
                    Component.text(extraAmountText, Palette.GOLD),
                    Component.text(" 추가 드랍", Palette.WHITE)
            ));
        }
        player.sendMessage(Component.text().append(
                Component.text("[도움말] ", Palette.GREEN),
                Component.text("다 자란 작물을 직접 수확하면 경험치를 얻습니다. " +
                        "레벨이 오를수록 추가 드랍 확률이 3%씩 늘고, 5레벨부터 1~2개, 10레벨엔 30% 확률로 2~3개!", Palette.GRAY)
        ));
        return true;
    }
}
