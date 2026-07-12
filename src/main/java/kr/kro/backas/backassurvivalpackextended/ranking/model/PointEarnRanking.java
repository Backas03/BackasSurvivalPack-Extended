package kr.kro.backas.backassurvivalpackextended.ranking.model;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.api.PlayerPointEarnEvent;
import kr.kro.backas.backassurvivalpackextended.api.UserDataPreLoadDoneEvent;
import kr.kro.backas.backassurvivalpackextended.point.PointManager;
import kr.kro.backas.backassurvivalpackextended.ranking.AbstractRanking;
import kr.kro.backas.backassurvivalpackextended.ranking.data.IntegerRankData;
import kr.kro.backas.backassurvivalpackextended.user.User;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataPoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PointEarnRanking extends AbstractRanking<IntegerRankData> {
    @Override
    public @NotNull String getName() {
        return "잠수포인트";
    }

    @Override
    public Listener getUpdateListener() {
        return new Listener() {
            @EventHandler
            public void onLoad(ServerLoadEvent event) {
                for (OfflinePlayer ofp : Bukkit.getOfflinePlayers()) {
                    User user = BackasSurvivalPackExtended.getUserManager()
                            .newInstance(ofp);
                    if (getRankData(ofp.getUniqueId()) != null) {
                        continue;
                    }
                    IntegerRankData data = new IntegerRankData(
                            ofp.getUniqueId(),
                            ofp.getName(),
                            user.getDataContainer()
                                    .getOrLoad(UserDataPoint.class)
                                    .getTotalEarned()
                    );
                    ranks.add(data);
                }
            }

            @EventHandler
            public void onPointEarn(PlayerPointEarnEvent event) {
                Player player = event.getPlayer();
                IntegerRankData data = getRankData(player);
                if (data != null) data.setAmount(event.getTotalEarned());
                else {
                    data = new IntegerRankData(
                            player,
                            event.getTotalEarned()
                    );
                    ranks.add(data);
                }
            }

            @EventHandler
            public void onDataLoaded(UserDataPreLoadDoneEvent event) {
                Player player = event.getUser().getPlayer();
                if (player == null) return;
                IntegerRankData data = getRankData(player);
                if (data != null) return;

                User user = BackasSurvivalPackExtended.getUserManager()
                        .newInstance(player);
                data = new IntegerRankData(
                        player,
                        user.getDataContainer()
                                .getOrLoad(UserDataPoint.class)
                                .getTotalEarned()
                );
                if (getRankData(player) == null) {
                    ranks.add(data);
                }
            }
        };
    }

    @Override
    protected void load() {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            User user = BackasSurvivalPackExtended.getUserManager()
                    .newInstance(offlinePlayer);
            IntegerRankData data = new IntegerRankData(
                    offlinePlayer.getUniqueId(),
                    offlinePlayer.getName(),
                    user.getDataContainer()
                            .getOrLoad(UserDataPoint.class)
                            .getTotalEarned()
            );
            ranks.add(data);
        }
    }

    @Override
    public void send(Player player, int displayAmount, int page) {
        if (displayAmount < 1) displayAmount = 1;

        int maxPage = (int) Math.ceil((double) ranks.size() / displayAmount);
        Component previousPageMessage = page <= 1 ? Component.empty() :
                Component.text("[이전] ", Palette.RED)
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.text("클릭하여 이전 페이지로 이동합니다.\n" +
                                        "/ranking " + getName() + " " + (page - 1)
                                )))
                        .clickEvent(ClickEvent.runCommand("/ranking " + getName() + " " + (page - 1)));
        Component nextPageMessage = page >= maxPage ? Component.empty() :
                Component.text().append(
                        Component.space(),
                        Component.text("[다음]", Palette.GREEN)
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.text("클릭하여 다음 페이지로 이동합니다.\n" +
                                                "/ranking " + getName() + " " + (page + 1)
                                        )))
                                .clickEvent(ClickEvent.runCommand("/ranking " + getName() + " " + (page + 1)))
                ).build();

        player.sendMessage(Component.text().append(
                        Component.text("[정보] ", Palette.GOLD),
                        Component.text(getName() + " 획득량", Palette.WHITE),
                        Component.space(),
                        previousPageMessage,
                        Component.text("[" + page + "/" + maxPage + "]", Palette.GRAY),
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
                    .append(Component.text("[#" + (rankIndex + 1) + "] ", Palette.GREEN))
                    .append(Component.text(data.getPlayerName(), Palette.AQUA))
                    .append(Component.text(" - ", Palette.GRAY))
                    .append(Component.text(String.format("%,d", data.getAmount()) + PointManager.POINT_UNIT, Palette.YELLOW))
            );
        }
        int playerRank = getRank(player);
        if (playerRank == -1) {
            player.sendMessage(Component.text("당신의 순위가 아직 책정되지 않았습니다.", Palette.RED));
            return;
        }
        player.sendMessage(Component.text().append(
                Component.text("당신의 순위: ", Palette.WHITE),
                Component.text(playerRank + "위", Palette.GOLD),
                Component.text("(" + String.format("%,d", getRankData(player).getAmount()) + PointManager.POINT_UNIT + ")", Palette.GRAY))
        );
    }
}
