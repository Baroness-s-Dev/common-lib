package ru.baronessdev.lib.common.cloud;

import co.aikar.commands.PaperCommandManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.lib.common.cloud.linked.LinkedPlugin;
import ru.baronessdev.lib.common.cloud.linked.LinkedPluginFactory;
import ru.baronessdev.lib.common.cloud.menu.CloudMenuManager;
import ru.baronessdev.lib.common.cloud.update.UpdateHandler;
import ru.baronessdev.lib.common.cloud.update.UpdateHandlerAction;
import ru.baronessdev.lib.common.cloud.update.UpdateHandlerFactory;
import ru.baronessdev.lib.common.cloud.update.UpdateHandlerListener;
import ru.baronessdev.lib.common.log.Logger;
import ru.baronessdev.lib.common.update.UpdateCheckException;
import ru.baronessdev.lib.common.update.UpdateCheckerUtil;
import ru.baronessdev.lib.common.util.ResourceUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaronessCloud {

    private static volatile BaronessCloud instance;
    private YamlConfiguration config;
    private boolean disabled;

    private final List<Index> indexList = new ArrayList<>();
    private final List<LinkedPlugin> pluginList = new ArrayList<>();

    private static int updateCount = 0;

    private UpdateHandler currentHandler;

    @SuppressWarnings("unused")
    public static BaronessCloud getInstance(JavaPlugin plugin) {
        BaronessCloud localInstance = instance;
        if (localInstance == null) {
            synchronized (BaronessCloud.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new BaronessCloud().setup(plugin);
                }
            }
        }
        return localInstance.addPlugin(plugin);
    }

    private synchronized BaronessCloud addPlugin(JavaPlugin plugin) {
        if (isDisabled()) return this;
        String pluginName = plugin.getName();

        // prevents double usage from same plugin
        if (pluginList.stream().anyMatch(linkedPlugin -> linkedPlugin.getBasePlugin().getName().equals(pluginName)))
            return this;

        // checks whether the plugin is in the indexes
        Index index = indexList.stream().filter(in -> in.getName().equals(pluginName)).findFirst().orElse(null);
        Validate.notNull(index, pluginName + " is not in BaronessCloud indexes");

        UpdateCheckerUtil.checkAsynchronously(index.getUrl(), (latestVersion) -> {

            // adding plugin to linked plugin list after collecting latest version data
            pluginList.add(LinkedPluginFactory.create(index, plugin, latestVersion, config));
            indexList.remove(index);

            buildMenu();

        }, this::logVersionCheckException);
        return this;
    }

    @SuppressWarnings({"unused"})
    public synchronized void addUpdateHandler(JavaPlugin plugin, UpdateHandler handler) {
        if (isDisabled()) return;

        if (currentHandler != null && handler.getHandlerPriority().higher(currentHandler.getHandlerPriority()))
            return;

        UpdateHandlerListener listener = new UpdateHandlerListener();
        handler.setListener(listener);
        Arrays.stream(handler.getEvents()).forEach(clazz -> Bukkit.getPluginManager().registerEvent(
                clazz,
                listener,
                handler.getEventPriority(),
                (l, event) -> UpdateHandlerAction.defaultAction(event, handler),
                plugin,
                handler.isIgnoreCancelled()
        ));

        if (currentHandler != null) currentHandler.deactivate();
        currentHandler = handler;
    }

    private BaronessCloud setup(JavaPlugin plugin) {
        setupConfig(plugin);
        UpdateHandlerFactory.setConfig(config);
        if (isDisabled()) return this;

        setupIndexes();
        setupCommand(plugin);
        return this;
    }

    private void setupIndexes() {
        // downloading indexes
        File indexesFile = new File("plugins" + File.separator + "BaronessCloud" + File.separator + "indexes");
        try {
            Files.copy(new URL("https://mirror.baronessdev.ru/BaronessCloud/indexes.yml").openStream(), indexesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // loading indexes
        YamlConfiguration indexConfig = YamlConfiguration.loadConfiguration(indexesFile);
        indexConfig.getKeys(false).forEach(key -> indexList.add(new Index(
                key,
                indexConfig.getString(key + ".url"),
                indexConfig.getStringList(key + ".description"),
                indexConfig.getString(key + ".material"),
                indexConfig.getString(key + ".fallbackMaterial"),
                indexConfig.getStringList(key + ".depends"),
                indexConfig.getStringList(key + ".sync")
        )));

        //noinspection ResultOfMethodCallIgnored
        indexesFile.delete();
    }

    private void setupCommand(JavaPlugin plugin) {
        if (!config.getBoolean("command.enabled")) return;

        PaperCommandManager manager = new PaperCommandManager(plugin);
        manager.registerCommand(new CloudCommand(config.getString("command.permission"), config.getString("command.no-permission-message")));
    }

    private void setupConfig(JavaPlugin plugin) {
        File configFile = new File("plugins" + File.separator + "BaronessCloud" + File.separator + "config.yml");
        ResourceUtil.saveResource(plugin, "cloudConfig.yml", configFile, false);

        config = YamlConfiguration.loadConfiguration(configFile);
        disabled = !config.getBoolean("enabled");
    }

    private void logVersionCheckException(UpdateCheckException e) {
        UpdateCheckerUtil.logDefaultExceptionError(new Logger.Builder("BaronessCloud").build(), e);
    }

    private void buildMenu() {
        CloudMenuManager.build(new ArrayList<>(pluginList), new ArrayList<>(indexList), config);
    }

    private boolean isDisabled() {
        return disabled;
    }

    public static void setUpdateCount(int updateCount) {
        BaronessCloud.updateCount = updateCount;
    }

    public static int getUpdateCount() {
        return updateCount;
    }
}
