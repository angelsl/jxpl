package org.angelsl.bukkit.jxpl;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JxplPlugin extends JavaPlugin {

    boolean loaded = false;
    private static Logger l = Logger.getLogger("Minecraft.JxplPlugin");

    private static File scriptsDir = null;
    private static ArrayList<ScriptPlugin> loadedPlugins = new ArrayList<ScriptPlugin>();

    public static File getScriptsDir() {
        return scriptsDir;
    }

    public static ArrayList<ScriptPlugin> getLoadedPlugins() {
        return loadedPlugins;
    }

    public void onDisable() {
        for (ScriptPlugin sp : loadedPlugins) {
            getServer().getPluginManager().disablePlugin(sp);
        }
    }

    public void onEnable() {
        if (!loaded) {
            l.log(Level.INFO, "Initialising jxpl...");
            scriptsDir = new File(getConfiguration().getString("scripts-dir", "scripts"));
            this.getServer().getPluginManager().RegisterInterface(ScriptLoader.class);
            if (scriptsDir.exists() && !scriptsDir.isDirectory()) scriptsDir.delete();
            if (!scriptsDir.exists()) scriptsDir.mkdir();
            this.getServer().getPluginManager().loadPlugins(scriptsDir);
        }
        for (Plugin p : loadedPlugins) {
            getServer().getPluginManager().enablePlugin(p);
        }
        loaded = true;
    }

}
