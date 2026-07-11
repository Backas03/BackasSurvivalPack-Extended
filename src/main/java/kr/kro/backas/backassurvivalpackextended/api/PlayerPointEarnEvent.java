package kr.kro.backas.backassurvivalpackextended.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerPointEarnEvent extends Event {
    public static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int earnedAmount;
    private final int totalEarned;

    public PlayerPointEarnEvent(Player player, int earnedAmount, int totalEarned) {
        this.player = player;
        this.earnedAmount = earnedAmount;
        this.totalEarned = totalEarned;
    }

    public Player getPlayer() {
        return player;
    }

    public int getEarnedAmount() {
        return earnedAmount;
    }

    public int getTotalEarned() {
        return totalEarned;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
