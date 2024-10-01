package kr.kro.backas.backassurvivalpackextended.ranking.model;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.ranking.AbstractRanking;
import kr.kro.backas.backassurvivalpackextended.ranking.data.DoubleRankData;
import kr.kro.backas.backassurvivalpackextended.user.User;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoney;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoneyUse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MoneyUseRanking extends AbstractRanking<DoubleRankData> {
    @Override
    public @NotNull String getName() {
        return "돈사용량";
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
                    DoubleRankData data = new DoubleRankData(
                            ofp.getUniqueId(),
                            ofp.getName(),
                            user.getDataContainer()
                                    .getOrLoad(UserDataMoneyUse.class)
                                    .getAmount()
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
        List<DoubleRankData> dataList = new ArrayList<>(ranks);
        for (int rankIndex = (page - 1) * displayAmount;
             rankIndex < page * displayAmount;
             rankIndex++
        ) {
            if (rankIndex >= ranks.size()) break;

            DoubleRankData data = dataList.get(rankIndex);
            player.sendMessage(Component.text()
                    .append(Component.text("[#" + (rankIndex + 1) + "] ", NamedTextColor.GREEN))
                    .append(Component.text(data.getPlayerName(), NamedTextColor.AQUA))
                    .append(Component.text(" - ", NamedTextColor.GRAY))
                    .append(Component.text(data.getAmount() + BackasSurvivalPackExtended.MONEY_UNIT, NamedTextColor.YELLOW))
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
                Component.text("(" + getRankData(player).getAmount() + BackasSurvivalPackExtended.MONEY_UNIT + ")", NamedTextColor.GRAY))
        );
    }

    @Override
    protected void load() {
        ranks.clear();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            User user = BackasSurvivalPackExtended.getUserManager()
                    .newInstance(offlinePlayer);
            DoubleRankData data = new DoubleRankData(
                    offlinePlayer.getUniqueId(),
                    offlinePlayer.getName(),
                    user.getDataContainer()
                            .getOrLoad(UserDataMoneyUse.class)
                            .getAmount()
            );
            ranks.add(data);
        }
    }
}
