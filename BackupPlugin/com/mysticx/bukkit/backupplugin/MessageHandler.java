package com.mysticx.bukkit.backupplugin;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to handle log messages and broadcasts
 *
 * @author MysticX
 */
public class MessageHandler {

    // server
    private static Server server;

    // Minecraft logger
    private static Logger l = Logger.getLogger("Minecraft");

    // message prefixes and stuff
    private static final ChatColor BROADCAST_COLOR = ChatColor.GREEN;
    private static final String PLUGIN_PREFIX = "[BackupPlugin] ";
    private static Level logLevel = Level.INFO;

    /**
     * Logs a message with PLUGIN_PREFIX
     *
     * @param level
     * @param message
     * @param thrown
     */
    protected static void log(Level level, String message, Throwable thrown) {
        if (compareLevel(level))
            l.log(level, addPrefix(message), thrown);
    }

    /**
     * Logs a message with PLUGIN_PREFIX
     *
     * @param level
     * @param message
     */
    protected static void log(Level level, String message) {
        //TODO quick hack for output < INFO
        if ((level == Level.FINE || level == Level.FINEST) && compareLevel(level))
            l.log(Level.INFO, addPrefix("[DEBUG] " + message));

        if (compareLevel(level))
            l.log(level, addPrefix(message));
    }


    /**
     * Logs a info with PLUGIN_PREFIX
     *
     * @param message
     */
    protected static void info(String message) {
        if (compareLevel(Level.INFO))
            l.info(addPrefix(message));
    }

    /**
     * Logs a warning with PLUGIN_PREFIX
     *
     * @param message
     */
    protected static void warning(String message) {
        if (compareLevel(Level.WARNING))
            l.warning(addPrefix(message));
    }

    /**
     * broadcasts an ingame message to all players
     *
     * @param message
     */
    protected static void broadcast(String message) {
        server.broadcastMessage(addPrefix(BROADCAST_COLOR + message));
//		for (Player p : etc.getServer().getPlayerList()) {
//			p.sendMessage(addPrefix(BROADCAST_COLOR + message));
//		}
    }

    /**
     * broadcasts an ingame message to a given player
     *
     * @param player
     * @param message
     */
    protected static void broadcast(Player p, String message) {
        p.sendMessage(addPrefix(BROADCAST_COLOR + message));
    }

    /**
     * adds given prefix to given String
     *
     * @param prefix
     * @param message
     * @return modified String
     */
    public static String addPrefix(String prefix, String message) {
        return prefix + message;
    }

    /**
     * Sets LogLevel for this MessageHandler
     *
     * @param level
     */
    public static void setLogLevel(Level loglevel) {
        logLevel = loglevel;
//		l.setLevel(logLevel);
    }

    /**
     * Sets LogLevel for this MessageHandler
     *
     * @param level
     */
    public static boolean setLogLevel(String loglevel) {
        try {
            Level level = Level.parse(loglevel);
            MessageHandler.setLogLevel(level);
            return true;
        } catch (Exception e) {
            MessageHandler.warning("Failed to parse log-level, using default.");
            return false;
        }
    }

    /**
     * adds PLUGIN_PREFIX and calling class to given String
     *
     * @param message
     * @return modified String
     */
    private static String addPrefix(String message) {
        return PLUGIN_PREFIX + "(" + getCallingClassName(4) + ") " + message;
    }

    /**
     * Compares two log levels
     *
     * @param arg0
     * @return true, if logging is enabled for this level
     */
    private static boolean compareLevel(Level arg0) {
        return (arg0.intValue() >= logLevel.intValue());
    }

    /**
     * @param grade grade of call
     * @return calling class name
     */
    private static String getCallingClassName(int grade) {
        String s = Thread.currentThread().getStackTrace()[grade].getClassName();
        return s;
    }

    protected static void setServer(Server instance) {
        server = instance;
    }

}
