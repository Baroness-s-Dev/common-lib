package ru.baronessdev.lib.common.cloud.linked;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.baronessdev.lib.common.cloud.BaronessCloud;
import ru.baronessdev.lib.common.cloud.Index;
import ru.baronessdev.lib.common.log.Logger;
import ru.baronessdev.lib.common.update.UpdateCheckerUtil;
import ru.baronessdev.lib.common.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class LinkedPluginFactory {

    public static LinkedPlugin create(@NotNull Index index, @NotNull JavaPlugin plugin, int latestVersion, @NotNull YamlConfiguration config) {
        // getting current version
        final int currentVersion = Integer.parseInt(plugin.getDescription().getVersion());

        // checking for updates
        UpdateCheckerUtil.UpdateType updateType = UpdateCheckerUtil.getUpdateType(plugin, currentVersion, latestVersion);
        final boolean hasUpdate = updateType == UpdateCheckerUtil.UpdateType.AVAILABLE;

        // material
        Material baseMaterial = Material.getMaterial(index.getMaterial());
        Material material = (baseMaterial == null) ? Material.getMaterial(index.getFallbackMaterial()) : baseMaterial;

        // name
        String name = ChatColor.GOLD + index.getName();

        // lore

            // description
        List<String> lore = new ArrayList<>(index.getDescription());
        lore.add("");

            // sync
        if (!index.getSync().isEmpty()) {
            lore.add(config.getString("icon.sync"));
            for (String syncedPlugin : index.getSync()) {
                ChatColor color = (Bukkit.getPluginManager().getPlugin(syncedPlugin) != null)
                        ? ChatColor.YELLOW
                        : ChatColor.GRAY;
                lore.add(ChatColor.WHITE + " - " + color + syncedPlugin);
            }
            lore.add("");
        }

            // versions
        lore.add(String.format(config.getString("icon.current-version"), currentVersion));
        lore.add(String.format(config.getString("icon.actual-version"), latestVersion));
        lore.add("");

            // update status
        lore.add(getUpdateStatusText(updateType, config));
        lore.add("");

            // link
        lore.add(config.getString("icon.link"));

        // building ItemStack
        ItemStack itemStack = new ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .build();

        if (hasUpdate) {
            BaronessCloud.setUpdateCount(BaronessCloud.getUpdateCount() + 1);
            UpdateCheckerUtil.logDefaultUpdate(new Logger.Builder("[" + plugin.getName() + "]").build(), plugin, latestVersion);
        }

        return new LinkedPlugin(plugin, itemStack, index.getUrl(), hasUpdate, index.getDescription());
    }

    private static String getUpdateStatusText(@NotNull UpdateCheckerUtil.UpdateType updateType, @NotNull YamlConfiguration config) {
        switch (updateType) {
            case AVAILABLE:
                return config.getString("icon.update-available");
            case UNAVAILABLE:
                return config.getString("icon.update-unavailable");
            case FAILED:
                return config.getString("icon.update-failed");
            default:
                return "";
        }
    }
}
