package kr.kro.backas.backassurvivalpackextended.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerSendMoneyEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player from;
    private final Player to;
    private final int amount;

    public PlayerSendMoneyEvent(Player from, Player to, int amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public Player getFrom() {
        return from;
    }

    public Player getTo() {
        return to;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
