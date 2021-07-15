package ru.baronessdev.lib.common.cloud.update;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.baronessdev.lib.common.util.SmartMessagesBuilder;

public class UpdateHandlerFactory {

    private static YamlConfiguration config;

    @SuppressWarnings("unchecked")
    public static UpdateHandlerBuilder create(@NotNull JavaPlugin plugin, @NotNull UpdateHandlerPriority handlerPriority, @NotNull Class<? extends Event>... events) {
        return new UpdateHandlerBuilder(plugin, handlerPriority, events);
    }

    @SuppressWarnings({"unchecked", "unused"})
    public static UpdateHandler createDefault(@NotNull JavaPlugin plugin) {
        return create(plugin, UpdateHandlerPriority.DEFAULT, PlayerJoinEvent.class).build();
    }

    protected static TextComponent getDefaultMessage() {
        ClickEvent.Action action = ClickEvent.Action.RUN_COMMAND;
        try {
            action = ClickEvent.Action.valueOf(config.getString("update-notify.click-action"));
        } catch (Exception ignored) {
        }

        return new SmartMessagesBuilder(config.getString("update-notify.message"))
                .setHoverEvent(HoverEvent.Action.SHOW_TEXT, config.getString("update-notify.hover"))
                .setClickEvent(action, config.getString("update-notify.click-value")).getMessage();
    }

    protected static String getDefaultPermission() {
        return config.getString("update-notify.permission");
    }

    public static void setConfig(YamlConfiguration config) {
        UpdateHandlerFactory.config = config;
    }
}
