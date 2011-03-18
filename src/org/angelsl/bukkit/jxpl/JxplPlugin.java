package org.angelsl.bukkit.jxpl;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JxplPlugin extends JavaPlugin {

    private static boolean _interfaceRegistered = false; // fuck bukkit devs
    private boolean _thisInitialised = false;
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
        if (!_thisInitialised) {
            if (loadedPlugins.size() > 0) {
                l.log(Level.INFO, "jxpl was reloaded. Clearing loaded scripts...");
                loadedPlugins.clear();
            }
            l.log(Level.INFO, "Initialising jxpl...");
            scriptsDir = new File(getConfiguration().getString("scripts-dir", "scripts"));
            if (!_interfaceRegistered) {
                this.getServer().getPluginManager().registerInterface(ScriptLoader.class);
                _interfaceRegistered = true;
            }
            if (scriptsDir.exists() && !scriptsDir.isDirectory()) scriptsDir.delete();
            if (!scriptsDir.exists()) scriptsDir.mkdir();
            this.getServer().getPluginManager().loadPlugins(scriptsDir);
            _thisInitialised = true;
        }
        for (Plugin p : loadedPlugins) {
            getServer().getPluginManager().enablePlugin(p);
        }

    }

}
