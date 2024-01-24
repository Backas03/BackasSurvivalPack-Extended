package kr.kro.backas.backassurvivalpackextended.ranking;

import org.bukkit.event.Event;

public abstract class ReloadCondition<T extends Event> {
    private final Class<T> when;

    public ReloadCondition(Class<T> when) {
        this.when = when;
    }

    public Class<T> getWhen() {
        return when;
    }

    public abstract void reload(T event);
}
