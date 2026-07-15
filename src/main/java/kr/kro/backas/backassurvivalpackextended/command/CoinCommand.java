package kr.kro.backas.backassurvivalpackextended.command;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.coin.CoinListInventory;
import kr.kro.backas.backassurvivalpackextended.coin.CoinService;
import kr.kro.backas.backassurvivalpackextended.coin.MarketRegistry;
import kr.kro.backas.backassurvivalpackextended.coin.UpbitMarket;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CoinCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length == 0) {
            CoinListInventory.open(player, 1, null);
            return true;
        }
        if (args[0].equals("알림")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("명령어 사용법: /" + label + " 알림 [코인 이름]", Palette.RED));
                return false;
            }
            UpbitMarket market = MarketRegistry.resolve(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            if (market == null) {
                player.sendMessage(Component.text("해당 코인을 찾을 수 없습니다.", Palette.RED));
                return false;
            }
            BackasSurvivalPackExtended.getCoinService().toggleLive(player, market);
            return true;
        }

        String input = String.join(" ", args);
        UpbitMarket market = MarketRegistry.resolve(input);
        if (market != null) {
            CoinService.sendDetail(player, market);
            return true;
        }

        List<UpbitMarket> candidates = MarketRegistry.search(input);
        if (candidates.isEmpty()) {
            player.sendMessage(Component.text().append(
                    Component.text("[코인] ", Palette.GOLD),
                    Component.text("'" + input + "' 코인을 찾을 수 없습니다.", Palette.RED)
            ));
            return false;
        }
        player.sendMessage(Component.text().append(
                Component.text("[코인] ", Palette.GOLD),
                Component.text("이 코인을 찾으셨나요? (클릭)", Palette.GRAY)
        ));
        for (int i = 0; i < Math.min(5, candidates.size()); i++) {
            UpbitMarket candidate = candidates.get(i);
            player.sendMessage(Component.text(" - " + candidate.getKoreanName() + " (" + candidate.getSymbol() + ")", Palette.AQUA)
                    .hoverEvent(HoverEvent.showText(Component.text("/" + label + " " + candidate.getKoreanName())))
                    .clickEvent(ClickEvent.runCommand("/" + label + " " + candidate.getKoreanName())));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String input = args.length == 0 ? "" : args[args.length - 1];
        boolean firstArg = args.length <= 1;

        List<String> suggestions = new ArrayList<>();
        if (firstArg && "알림".startsWith(input)) {
            suggestions.add("알림");
        }
        String lower = input.toLowerCase(Locale.ROOT);
        for (UpbitMarket market : MarketRegistry.getAll()) {
            if (suggestions.size() >= 20) break;
            if (market.getKoreanName().startsWith(input)) {
                suggestions.add(market.getKoreanName());
            } else if (!input.isEmpty() && market.getSymbol().toLowerCase(Locale.ROOT).startsWith(lower)) {
                suggestions.add(market.getSymbol());
            }
        }
        return suggestions;
    }
}
