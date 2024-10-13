package kr.kro.backas.backassurvivalpackextended.coin;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataCoin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class CoinManager {
    public static final String UPBIT_TICKER_END_POINT = "/v1/ticker";

    private final long tick = 5;
    private BukkitTask task;

    private final List<Player> senders = new ArrayList<>();

    private double bitcoin;
    private int ticks = 0;
    private NamedTextColor color = NamedTextColor.WHITE;

    public void start() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
            try {
                bitcoin = CoinCostCache.bitcoin;
                CoinCostCache.bitcoin = new HttpRequest(
                        UpbitAPI.URL + UPBIT_TICKER_END_POINT,
                        "GET",
                        "?markets=" + Market.BTC
                ).getResponse().trade_price;

                if (bitcoin > CoinCostCache.bitcoin) {
                    color = NamedTextColor.RED;
                    ticks = 0;
                } else if (bitcoin < CoinCostCache.bitcoin) {
                    color = NamedTextColor.GREEN;
                    ticks = 0;
                } else if (ticks > 5) {
                    color = NamedTextColor.WHITE;
                    ticks = 0;
                }
                for (Player sender : senders) {
                    send(sender);
                }
                ticks++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0L, tick);
    }


    private final TextColor live = TextColor.fromHexString("#86E57F");
    public void send(Player player) {
        player.sendActionBar(Component.text().append(
                Component.text("[LIVE] ", live),
                Component.text("비트코인 " , NamedTextColor.GOLD),
                Component.text(String.format("%,3.0f원", CoinCostCache.bitcoin), color)
        ));
    }

    public void toggleSender(Player player) {
        if (senders.contains(player)) {
            senders.remove(player);
            player.sendMessage(Component.text("이제부터 코인 시세 알림을 받지 않습니다.", NamedTextColor.RED));
        } else {
            senders.add(player);
            player.sendMessage(Component.text("이제부터 코인 시세 알림을 받습니다.", NamedTextColor.GREEN));
        }
    }
}
