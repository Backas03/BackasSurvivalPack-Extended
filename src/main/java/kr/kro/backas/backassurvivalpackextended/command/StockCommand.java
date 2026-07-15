package kr.kro.backas.backassurvivalpackextended.command;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.stock.Stock;
import kr.kro.backas.backassurvivalpackextended.stock.StockListInventory;
import kr.kro.backas.backassurvivalpackextended.stock.StockMarket;
import kr.kro.backas.backassurvivalpackextended.stock.StockQuoteService;
import kr.kro.backas.backassurvivalpackextended.stock.StockRegistry;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class StockCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length == 0) {
            StockListInventory.openAsync(player, StockMarket.KOSPI, 1, null);
            return true;
        }
        if (args[0].equals("알림")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("명령어 사용법: /" + label + " 알림 [종목 이름]", Palette.RED));
                return false;
            }
            Stock stock = StockRegistry.resolve(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            if (stock == null) {
                player.sendMessage(Component.text("해당 종목을 찾을 수 없습니다.", Palette.RED));
                return false;
            }
            BackasSurvivalPackExtended.getStockQuoteService().toggleLive(player, stock);
            return true;
        }

        String input = String.join(" ", args);
        Stock stock = StockRegistry.resolve(input);
        if (stock != null) {
            Bukkit.getScheduler().runTaskAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
                try {
                    StockQuoteService.fetchDetail(stock);
                } catch (Exception ignored) {
                }
                StockQuoteService.sendDetail(player, stock);
            });
            return true;
        }

        List<Stock> candidates = new ArrayList<>();
        for (StockMarket market : StockMarket.values()) {
            candidates.addAll(StockRegistry.search(market, input));
            if (candidates.size() >= 5) break;
        }
        if (candidates.isEmpty()) {
            player.sendMessage(Component.text().append(
                    Component.text("[주식] ", Palette.GOLD),
                    Component.text("'" + input + "' 종목을 찾을 수 없습니다.", Palette.RED)
            ));
            return false;
        }
        player.sendMessage(Component.text().append(
                Component.text("[주식] ", Palette.GOLD),
                Component.text("이 종목을 찾으셨나요? (클릭)", Palette.GRAY)
        ));
        for (int i = 0; i < Math.min(5, candidates.size()); i++) {
            Stock candidate = candidates.get(i);
            player.sendMessage(Component.text(" - " + candidate.name() + " (" + candidate.market().getDisplayName()
                            + " · " + candidate.code() + ")", Palette.AQUA)
                    .hoverEvent(HoverEvent.showText(Component.text("/" + label + " " + candidate.code())))
                    .clickEvent(ClickEvent.runCommand("/" + label + " " + candidate.code())));
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
        if (input.isEmpty()) {
            return suggestions;
        }
        String upper = input.toUpperCase(Locale.ROOT);
        for (StockMarket market : StockMarket.values()) {
            for (Stock stock : StockRegistry.getAll(market)) {
                if (suggestions.size() >= 20) return suggestions;
                if (stock.name().startsWith(input)) {
                    suggestions.add(stock.name());
                } else if (market == StockMarket.NASDAQ && stock.code().startsWith(upper)) {
                    suggestions.add(stock.code());
                }
            }
        }
        return suggestions;
    }
}
