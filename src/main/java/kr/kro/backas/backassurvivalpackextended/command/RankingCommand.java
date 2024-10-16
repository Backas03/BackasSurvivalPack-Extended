package kr.kro.backas.backassurvivalpackextended.command;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.ranking.AbstractRanking;
import kr.kro.backas.backassurvivalpackextended.ranking.RankingManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RankingCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text("아래는 조회가능한 랭킹 목록입니다.", NamedTextColor.YELLOW));
            for (AbstractRanking<?> ranking : BackasSurvivalPackExtended.getRankingManager().getAllRankings()) {
                player.sendMessage(
                        Component.text().append(
                                        Component.text(ranking.getName(), NamedTextColor.WHITE),
                                        Component.space(),
                                        Component.text("[클릭하여 조회하기]", NamedTextColor.GREEN))
                                .hoverEvent(HoverEvent.showText(Component.text("/" + label + " " + ranking.getName())))
                                .clickEvent(ClickEvent.runCommand("/" + label + " " + ranking.getName()))
                );
            }
            player.sendMessage(Component.text().append(
                    Component.text("[도움말] ", NamedTextColor.AQUA),
                    Component.text("/" + label + " [이름]", NamedTextColor.WHITE),
                    Component.text(" - 순위를 조회합니다.", NamedTextColor.GRAY)
            ));
            RankingManager rankingManager = BackasSurvivalPackExtended.getRankingManager();
            player.sendMessage(Component.text().append(
                    Component.text("[마지막 업데이트] ", NamedTextColor.RED),
                    Component.text(rankingManager.getLastUpdate().format(DateTimeFormatter.ofPattern("HH시 mm분")), NamedTextColor.WHITE)
            ));
            LocalDateTime update = rankingManager.getLastUpdate().plus(Duration.ofSeconds(RankingManager.UPDATE_INTERVAL / 20));
            Duration diff = Duration.between(LocalDateTime.now(), update);
            player.sendMessage(Component.text().append(
                    Component.text("[랭킹 업데이트 까지] ", NamedTextColor.GOLD),
                    Component.text(diff.toHours() + "시간 " + diff.toMinutesPart() + "분 " + diff.toSecondsPart() + "초", NamedTextColor.WHITE)
            ));
            return false;
        }
        if (args[0].equals("update") && player.isOp()) {
            BackasSurvivalPackExtended.getRankingManager().update(false);
            player.sendMessage(Component.text("모든 랭킹이 갱신되었습니다.", NamedTextColor.GREEN));
            return false;
        }
        AbstractRanking<?> ranking = BackasSurvivalPackExtended.getRankingManager().getRanking(args[0]);
        if (ranking == null) {
            player.sendMessage(Component.text("해당 랭킹을 찾을 수 없습니다.", NamedTextColor.RED));
            return false;
        }
        int page = 1;
        if (args.length >= 2) {
            try {
                int temp = Integer.parseInt(args[1]);
                if (temp < 1) {
                    player.sendMessage(Component.text("페이지는 1 이상이어야 합니다.", NamedTextColor.RED));
                    return false;
                }
                page = temp;
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("페이지는 숫자여야 합니다.", NamedTextColor.RED));
                return false;
            }
        }
        ranking.send(player, 5, page);
        return false;
    }
}
