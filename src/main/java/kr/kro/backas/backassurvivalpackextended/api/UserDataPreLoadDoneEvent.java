package kr.kro.backas.backassurvivalpackextended.api;

import kr.kro.backas.backassurvivalpackextended.user.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class UserDataPreLoadDoneEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final User user;

    public UserDataPreLoadDoneEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
