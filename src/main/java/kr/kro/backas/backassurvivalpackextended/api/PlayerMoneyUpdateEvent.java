package kr.kro.backas.backassurvivalpackextended.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerMoneyUpdateEvent extends Event {
    public static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int oldAmount;
    private final int newAmount;

    public PlayerMoneyUpdateEvent(Player player, int oldAmount, int newAmount) {
        this.player = player;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
    }

    public Player getPlayer() {
        return player;
    }

    public int getOldAmount() {
        return oldAmount;
    }

    public int getNewAmount() {
        return newAmount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
