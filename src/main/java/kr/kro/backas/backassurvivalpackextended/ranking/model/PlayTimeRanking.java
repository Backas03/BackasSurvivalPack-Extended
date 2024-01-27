package kr.kro.backas.backassurvivalpackextended.ranking.model;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.ranking.AbstractRanking;
import kr.kro.backas.backassurvivalpackextended.ranking.data.DoubleRankData;
import kr.kro.backas.backassurvivalpackextended.ranking.data.IntegerRankData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayTimeRanking extends AbstractRanking<IntegerRankData> {
    @Override
    public @NotNull String getName() {
        return "플레이타임";
    }

    @Override
    public Listener getUpdateListener() {
        return null;
    }

    private void update() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            IntegerRankData data = getRankData(player.getUniqueId());
            if (data != null) data.setAmount(player.getStatistic(Statistic.PLAY_ONE_MINUTE));
            else {
                data = new IntegerRankData(
                        player,
                        player.getStatistic(Statistic.PLAY_ONE_MINUTE)
                );
                ranks.add(data);
            }
        }
    }

    @Override
    public void send(Player player, int displayAmount, int page) {
        update();
        if (displayAmount < 1) displayAmount = 1;

        int maxPage = (int) Math.ceil((double) ranks.size() / displayAmount);
        Component previousPageMessage = page <= 1 ? Component.empty() :
                Component.text("[이전] ", NamedTextColor.RED)
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.text("클릭하여 이전 페이지로 이동합니다.\n" +
                                        "/ranking " + getName() + " " + (page - 1)
                                )))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/ranking " + getName() + " " + (page - 1)));
        Component nextPageMessage = page >= maxPage ? Component.empty() :
                Component.text().append(
                        Component.space(),
                        Component.text("[다음]", NamedTextColor.GREEN)
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.text("클릭하여 다음 페이지로 이동합니다.\n" +
                                                "/ranking " + getName() + " " + (page + 1)
                                        )))
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/ranking " + getName() + " " + (page + 1)))
                ).build();

        player.sendMessage(Component.text().append(
                        Component.text("[정보] ", NamedTextColor.GOLD),
                        Component.text(getName(), NamedTextColor.WHITE),
                        Component.space(),
                        previousPageMessage,
                        Component.text("[" + page + "/" + maxPage + "]", NamedTextColor.GRAY),
                        nextPageMessage
                ).build()
        );
        List<IntegerRankData> dataList = new ArrayList<>(ranks);
        for (int rankIndex = (page - 1) * displayAmount;
             rankIndex < page * displayAmount;
             rankIndex++
        ) {
            if (rankIndex >= ranks.size()) break;

            IntegerRankData data = dataList.get(rankIndex);
            player.sendMessage(Component.text()
                    .append(Component.text("[#" + (rankIndex + 1) + "] ", NamedTextColor.GREEN))
                    .append(Component.text(data.getPlayerName(), NamedTextColor.AQUA))
                    .append(Component.text(" - ", NamedTextColor.GRAY))
                    .append(Component.text(convertTime(data.getAmount()), NamedTextColor.YELLOW))
            );
        }
        int playerRank = getRank(player);
        if (playerRank == -1) {
            player.sendMessage(Component.text("당신의 순위가 아직 책정되지 않았습니다.", NamedTextColor.RED));
            return;
        }
        player.sendMessage(Component.text().append(
                Component.text("당신의 순위: ", NamedTextColor.WHITE),
                Component.text(playerRank + "위", NamedTextColor.GOLD),
                Component.text("(" + convertTime(getRankData(player).getAmount()) + ")", NamedTextColor.GRAY))
        );
    }

    private String convertTime(int ticks) {
        int totalSeconds = ticks / 20;
        int seconds = totalSeconds % 60;
        int minute = (totalSeconds / 60) % 60;
        int hours = (totalSeconds / 3600) % 24;
        int days = (totalSeconds / 3600) / 24;
        if (days == 0) return hours + "시간 " + minute + "분 " + seconds + "초";
        else if (hours == 0) return minute + "분 " + seconds + "초";
        return days + "일 " + hours + "시간 " + minute + "분 " + seconds + "초";
    }

    @Override
    protected void load() {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            IntegerRankData data = getRankData(offlinePlayer.getUniqueId());
            if (data != null) data.setAmount(offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE));
            else {
                data = new IntegerRankData(
                        offlinePlayer,
                        offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE)
                );
                ranks.add(data);
            }
        }
    }
}
