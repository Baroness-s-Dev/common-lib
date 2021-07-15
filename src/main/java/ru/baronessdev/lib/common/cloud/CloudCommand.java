package ru.baronessdev.lib.common.cloud;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.baronessdev.lib.common.cloud.menu.CloudMenuManager;

@CommandAlias("baronesscloud")
public class CloudCommand extends BaseCommand {

    private final String permission;
    private final String permissionMessage;

    protected CloudCommand(String permission, String permissionMessage) {
        this.permission = permission;
        this.permissionMessage = permissionMessage;
    }

    @CatchUnknown
    @Default
    public void call(CommandSender sender) {
        Player p;
        try {
            p = (Player) sender;
        } catch (ClassCastException ignored) {
            return;
        }

        if (!p.hasPermission(permission)) {
            p.sendMessage(permissionMessage);
            return;
        }

        if (CloudMenuManager.handle(p)) {
            p.openInventory(CloudMenuManager.getMenu());
        }
    }
}
