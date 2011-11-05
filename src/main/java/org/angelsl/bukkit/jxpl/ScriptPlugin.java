/*
 * This file is part of jxpl.
 *
 * jxpl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jxpl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jxpl.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.angelsl.bukkit.jxpl;

import com.avaje.ebean.EbeanServer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.util.config.Configuration;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScriptPlugin implements Plugin {

    private boolean isEnabled = false;
    private final PluginLoader loader;
    private final Server server;
    private final File file;
    private final Map<String, Object> rdescription;
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private FileConfiguration config = null;
    private final File configFile;
    private Invocable sEngine;
    private PluginHelper helper;
    private Logger l;
    private boolean naggable = true;


    public ScriptPlugin(PluginLoader pluginLoader, Server instance, File plugin, ScriptEngine engine) throws InvalidDescriptionException {
        loader = pluginLoader;
        server = instance;
        file = plugin;
        rdescription = (Map<String, Object>) Utils.getOrExcept(engine, "SCRIPT_PDF");
        description = Utils.getPdfFromMap(rdescription);
        l = Logger.getLogger("Minecraft.JxplPlugin." + description.getName());
        dataFolder = initialiseDataFolder();
        helper = new PluginHelper();
        engine.put(Utils.getOrDefault(rdescription, "jxpl.helpervarname", "helper"), helper);
        engine.put(Utils.getOrDefault(rdescription, "jxpl.pluginvarname", "plugin"), this);
        engine.put(Utils.getOrDefault(rdescription, "jxpl.servervarname", "server"), server);
        sEngine = (Invocable) engine;
        if (dataFolder != null && Utils.getOrDefault(rdescription, "jxpl.hasconfig", false)) {
            configFile = new File(dataFolder, "config.yml");
        } else {
            configFile = null;
        }
    }

    private File initialiseDataFolder() {
        if (Utils.getOrDefault(rdescription, "jxpl.hasdatafolder", false)) {
            File tempFolder = new File(file.getParentFile(), description.getName());
            if (tempFolder.exists() && !tempFolder.isDirectory()) {
                l.log(Level.WARNING, String.format("Data folder for %s at path \"%s\" exists but is a file.", description.getName(), tempFolder.getAbsolutePath()));
            } else if ((tempFolder.exists() && tempFolder.isDirectory()) || !tempFolder.exists()) {
                if (!tempFolder.exists() && !tempFolder.mkdirs()) {
                    l.log(Level.WARNING, String.format("Failed to create data folder for %s at path \"%s\".", description.getName(), tempFolder.getAbsolutePath()));
                } else {
                    return tempFolder;
                }
            }
        }
        return null;
    }

    private YamlConfiguration getDefaultConfig() {
        try {
            Map<String, Object> defmap = (Map<String, Object>) Utils.getOrExcept((ScriptEngine) sEngine, "DEFAULT_CONFIG");
            YamlConfiguration yamldef = new YamlConfiguration();
            Method loadFromMap = YamlConfiguration.class.getDeclaredMethod("deserializeValues", Map.class, ConfigurationSection.class);
            loadFromMap.setAccessible(true);
            loadFromMap.invoke(yamldef, defmap, yamldef);
            return yamldef;
        } catch (IllegalArgumentException e) {
        } catch (Throwable t) {
            l.log(Level.SEVERE, "Failed to get default config!", t);
        }
        return null;
    }

    @Override
    public EbeanServer getDatabase() {
        return null;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return null;
    }

    @Override
    public void setNaggable(boolean canNag) {
        naggable = canNag;
    }

    @Override
    public boolean isNaggable() {
        return naggable;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return description;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    @Override
    public InputStream getResource(String filename) {
        try {
            return new FileInputStream(new File(dataFolder, filename));
        } catch (Throwable t) {
            l.log(Level.SEVERE, String.format("Failed to get resource \"%s\".", filename), t);
        }
        return null;
    }

    @Override
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            l.log(Level.SEVERE, "Failed to save configuration file.", e);
        }
    }

    @Override
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        YamlConfiguration defaults = getDefaultConfig();
        if (defaults != null) {
            config.setDefaults(defaults);
        }
    }

    @Override
    public PluginLoader getPluginLoader() {
        return loader;
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void onDisable() {
        isEnabled = false;
        tryInvoke("onDisable", false);
    }

    @Override
    public void onLoad() {
        tryInvoke("onLoad", true);
    }

    @Override
    public void onEnable() {
        isEnabled = true;
        tryInvoke("onEnable", false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        return (Boolean) tryInvoke("onCommand", false, sender, command, commandLabel, args);
    }

    public void reloadScript() {
        onDisable();
        ScriptEngine engine = ((ScriptLoader) loader).getScriptEngine(file);
        engine.put(Utils.getStringOrDefault(engine, "HELPER_VARIABLE_NAME", "helper"), helper);
        engine.put(Utils.getStringOrDefault(engine, "PLUGIN_VARIABLE_NAME", "plugin"), this);
        engine.put(Utils.getStringOrDefault(engine, "SERVER_VARIABLE_NAME", "server"), server);
        sEngine = (Invocable) engine;
        onEnable();
    }

    private Object tryInvoke(String f, boolean stfu, Object... p) {
        try {
            return sEngine.invokeFunction(f, p);
        } catch (Throwable e) {
            if (!stfu) {
                l.log(Level.WARNING, "Error while running " + f + " of script " + file.getName() + ".", e);
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public Invocable getScriptEngine() {
        return sEngine;
    }

    public class ScriptEventListener implements Listener {
        private String callback;

        public ScriptEventListener(String fn) {
            callback = fn;
        }

        public void onEvent(Event.Type type, Event args) {
            if (ScriptPlugin.this.isEnabled) {
                ScriptPlugin.this.tryInvoke(callback, false, type, args);
            }
        }
    }

    public class PluginHelper {
        /**
         * Register an event, specifying the function that should handle the event
         *
         * @param event        The type of the event that will trigger the function
         * @param priority     The priority of the event handler
         * @param functionName The name of the function that will handle the event
         */
        @SuppressWarnings("unused")
        public void registerEvent(Event.Type event, Event.Priority priority, String functionName) {
            ScriptPlugin.this.server.getPluginManager().registerEvent(event, new ScriptEventListener(functionName), priority, ScriptPlugin.this);
        }

        /**
         * Logs a message.
         *
         * @param l       The level of the message
         * @param message The message to be logged
         */
        @SuppressWarnings("unused")
        public void log(Level l, String message) {
            ScriptPlugin.this.l.log(l, String.format("[%s] %s", description.getName(), message));
        }

        /**
         * Logs a message.
         *
         * @param l       The level of the message
         * @param message The message to be logged
         * @param thrown  The exception thrown
         */
        @SuppressWarnings("unused")
        public void log(Level l, String message, Throwable thrown) {
            ScriptPlugin.this.l.log(l, String.format("[%s] %s", description.getName(), message), thrown);
        }

        /**
         * Includes a script, with a path either absolute, or relative to Bukkit's current working directory (should be the root directory)
         *
         * @param s The file name of the script to include
         * @return result Object of eval if successful, null otherwise
         */
        @SuppressWarnings("unused")
        public Object includeScript(String s) {
            return includeScript(new File(s));
        }

        /**
         * Includes a script.
         *
         * @param f The file describing the script file to be included
         * @return result Object of eval if successful, null otherwise
         */
        public Object includeScript(File f) {
            Object result = null;
            try {
                FileReader fr = new FileReader(f);
                try {
                    result = ((ScriptEngine) ScriptPlugin.this.sEngine).eval(fr);
                } catch (Throwable t) {
                    l.log(Level.WARNING, "Failed to include script " + f.getPath() + " from " + file.getPath(), t);
                }
                fr.close();
            } catch (Throwable t) {
                l.log(Level.WARNING, "Could not read file " + f.getPath() + " from " + file.getPath(), t);
            }
            return result;
        }

        /**
         * Gets the contents of a file.
         *
         * @param path The path of the file, either absolute, or relative to Bukkit's CWD
         * @return The file's contents, or null if an error occured, or the file does not exist.
         */
        @SuppressWarnings("unused")
        public String getFileContents(String path) {
            return getFileContents(new File(path));
        }

        /**
         * Gets the contents of a file.
         *
         * @param file The file describing the file whose contents are to be read
         * @return The file's contents, or null if an error occured, or the file does not exist.
         */
        public String getFileContents(File file) {
            StringBuffer sb = new StringBuffer(1024);
            FileReader fr = null;
            BufferedReader reader = null;
            try {
                fr = new FileReader(file);
                reader = new BufferedReader(fr);
                char[] chars = new char[1024];
                int numRead = 0;
                while ((numRead = reader.read(chars)) > -1) {
                    sb.append(String.valueOf(chars));
                }
            } catch (Throwable e) {
                return null;
            } finally {
                try {
                    fr.close();
                    reader.close();

                } catch (Throwable e) {
                }
            }
            return sb.toString();
        }
    }
}
