package org.angelsl.bukkit.javaxscriptloader;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ScriptLoaderPlugin extends JavaPlugin {

    boolean loaded = false;
    private static Logger l = Logger.getLogger("Minecraft.ScriptLoaderPlugin");

    private static File scriptsDir = null;
    private static ArrayList<ScriptPlugin> loadedPlugins = new ArrayList<ScriptPlugin>();

    public static File getScriptsDir() {
        return scriptsDir;
    }

    public static ArrayList<ScriptPlugin> getLoadedPlugins() {
        return loadedPlugins;
    }

    public ScriptLoaderPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    public void onDisable() {

        // TODO: remove loader interface & unload scripts.
        // TODO: right now we'll just disable plugins *sigh*
        for (ScriptPlugin sp : loadedPlugins) {
            getServer().getPluginManager().disablePlugin(sp);
        }

        /*try {
            SimplePluginManager.class.
            Field plugins = SimplePluginManager.class.getField("plugins");
            Field lookupNames = SimplePluginManager.class.getField("lookupNames");
            plugins.setAccessible(true);
            lookupNames.setAccessible(true);
            List<Plugin> rPlugins = (List<Plugin>)plugins.get(getServer().getPluginManager());
            Map<String, Plugin> rLookup = (Map<String, Plugin>) lookupNames.get(getServer().getPluginManager());

            for(ScriptPlugin sp : loadedPlugins)
            {
                getServer().getPluginManager().disablePlugin(sp);
                rPlugins.remove(sp);
                rLookup.remove(sp.getDescription().getName());
            }

            loadedPlugins.clear();
        } catch (Throwable e) {
            l.log(Level.SEVERE, "Failed to unload javax.script plugins!", e);
            throw new RuntimeException("Failed to unload javax.script plugins. Refuse to unload.", e);
        }*/

    }

    public void onEnable() {

        l.log(Level.INFO, "Initialising javax.script script loader ...");
        scriptsDir = new File(getConfiguration().getString("scripts-dir", "scripts"));
        if (!loaded) this.getServer().getPluginManager().RegisterInterface(ScriptLoader.class);
        if (scriptsDir.exists() && !scriptsDir.isDirectory()) scriptsDir.delete();
        if (!scriptsDir.exists()) scriptsDir.mkdir();
        Plugin[] loadeds = this.getServer().getPluginManager().loadPlugins(scriptsDir);
        for (Plugin p : loadeds) {
            getServer().getPluginManager().enablePlugin(p);
        }

        loaded = true;
    }

}
