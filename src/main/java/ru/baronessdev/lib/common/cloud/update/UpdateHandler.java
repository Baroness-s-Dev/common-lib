package ru.baronessdev.lib.common.cloud.update;

import lombok.Data;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Data
public class UpdateHandler {
    private final UpdateHandlerPriority handlerPriority;
    private final Class<? extends Event>[] events;
    private final TextComponent message;
    private final String method;
    private final EventPriority eventPriority;
    private final String permission;
    private final boolean ignoreCancelled;
    private final int delay;
    private final JavaPlugin plugin;

    private Listener listener;

    public void deactivate() {
        HandlerList.unregisterAll(listener);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}