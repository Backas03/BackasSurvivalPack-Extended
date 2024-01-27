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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeathRanking extends AbstractRanking<IntegerRankData> {
    @Override
    public @NotNull String getName() {
        return "데스";
    }

    @Override
    public Listener getUpdateListener() {
        return new Listener() {
            @EventHandler
            public void onDeath(PlayerDeathEvent event) {
                Player player = event.getPlayer();
                IntegerRankData data = getRankData(player);
                if (data != null) data.setAmount(player.getStatistic(Statistic.DEATHS));
                else {
                    data = new IntegerRankData(
                            player,
                            player.getStatistic(Statistic.DEATHS)
                    );
                    ranks.add(data);
                }
            }
        };
    }

    @Override
    public void send(Player player, int displayAmount, int page) {
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
                    .append(Component.text(data.getAmount() + " 데스", NamedTextColor.YELLOW))
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
                Component.text("(" + getRankData(player).getAmount() + " 데스)", NamedTextColor.GRAY))
        );
    }

    @Override
    protected void load() {
        for (OfflinePlayer ofp : Bukkit.getOfflinePlayers()) {
            IntegerRankData data = getRankData(ofp.getUniqueId());
            if (data != null) data.setAmount(ofp.getStatistic(Statistic.DEATHS));
            else {
                data = new IntegerRankData(
                        ofp,
                        ofp.getStatistic(Statistic.DEATHS)
                );
                ranks.add(data);
            }
        }
    }
}
