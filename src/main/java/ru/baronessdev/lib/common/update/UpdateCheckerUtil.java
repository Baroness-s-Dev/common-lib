package ru.baronessdev.lib.common.update;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.baronessdev.lib.common.log.LogLevel;
import ru.baronessdev.lib.common.log.Logger;

import java.io.IOException;

@SuppressWarnings("unused")
public class UpdateCheckerUtil {

    public static int check(@NotNull JavaPlugin plugin, @NotNull String url, Logger logger) throws UpdateCheckException {
        if (logger != null) {
            return checkWithLogger(plugin, url, logger);
        }

        int latest = getLatest(url);
        if (latest == -1) {
            throw new UpdateCheckException("Checker returned -1");
        }

        return (Integer.parseInt(plugin.getDescription().getVersion()) < latest) ? latest : -1;
    }

    private static int checkWithLogger(JavaPlugin plugin, String url, Logger logger) {
        int i = -1;
        try {
            i = UpdateCheckerUtil.check(plugin, url, null);
            if (i != -1) {
                logger.log(LogLevel.INFO, "New version found: v" + ChatColor.YELLOW + i + ChatColor.GRAY + " (Current: v" + plugin.getDescription().getVersion() + ")");
                logger.log(LogLevel.INFO, "Update now: " + ChatColor.AQUA + "market.baronessdev.ru");
            }
        } catch (UpdateCheckException e) {
            logger.log(LogLevel.ERROR, "Could not check for updates: " + e.getRootCause());
            logger.log(LogLevel.ERROR, "Please contact Baroness's Dev if this isn't your mistake.");
        }
        return i;
    }

    private static int getLatest(String url) throws UpdateCheckException {
        int version = -1;
        try {
            String result = EntityUtils.toString(HttpClients.createDefault().execute(
                    new HttpGet(url)
            ).getEntity());

            for (String s : result.split("\n")) {
                if (s.contains("model")) {
                    version = Integer.parseInt(s.replaceAll("[^0-9]", ""));
                    break;
                }
            }
        } catch (IOException e) {
            throw new UpdateCheckException(e.getCause().getMessage());
        }

        return version;
    }
}
