package com.mysticx.bukkit.backupplugin;

import com.nijiko.permissions.PermissionHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * Handle events for all Player related events
 *
 * @author MysticX
 */
public class BackupPluginPlayerListener extends PlayerListener {
    private final BackupPlugin plugin;

    public BackupPluginPlayerListener(BackupPlugin instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerCommand(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String[] split = event.getMessage().split(" ");

        // check for backup command
        if (split[0].equals("/backup") && canUseCommand(player, "backup")) {
            if (split.length > 2) {
                player.sendMessage(ChatColor.RED + "Correct usage is: /backup <force> (optional)");
            } else {
                boolean force = false;
                if (split.length == 2) force = Boolean.valueOf(split[1]);

                String broadcast = player.getName() + " triggered world backup.";
                MessageHandler.info(broadcast + " force = " + force);
                MessageHandler.broadcast(broadcast);

                plugin.performBackup(force);
            }

            event.setCancelled(true);
            return;
        } else if (split[0].equals("/map") && canUseCommand(player, "map")) {
            if (split.length > 2) {
                player.sendMessage(ChatColor.RED + "Correct usage is: /map <force> (optional)");
            } else {
                boolean force = false;
                if (split.length == 2) force = Boolean.valueOf(split[1]);

                String broadcast = player.getName() + " triggered world mapping.";
                MessageHandler.info(broadcast + " force = " + force);
                MessageHandler.broadcast(broadcast);

                plugin.performMapping(force);
            }

            event.setCancelled(true);
            return;
        } else if (split[0].equals("/breload") && canUseCommand(player, "reload")) {
            String broadcast = player.getName() + " triggered config reload.";
            MessageHandler.info(broadcast);
            MessageHandler.broadcast(broadcast);

            plugin.load();

            event.setCancelled(true);
            return;
        } else if (split[0].equals("/loglevel") && split.length == 2 && canUseCommand(player, "loglevel")) {
            MessageHandler.info(player.getName() + " is changing log level to " + split[1]);
            boolean b = MessageHandler.setLogLevel(split[1]);
            if (b) player.sendMessage("Done!");
            else player.sendMessage("Failed!");

            event.setCancelled(true);
            return;
        }

        // no match
        event.setCancelled(false);
    }

    /**
     * Checks if a given player can use a given command
     * (Tries to user Group Users Plugin first, own config only if there is no plugin)
     *
     * @param player
     * @param command
     * @return
     */
    private boolean canUseCommand(Player player, String command) {
        // check for groupUserPlugin
        if (BackupPlugin.getPermissions() != null) {
            PermissionHandler ph = BackupPlugin.getPermissions();
            return ph.has(player, "backupplugin." + command);
        }
        // no permissions ...
        return false;
    }

}

