package org.angelsl.bukkit.jxpl;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.util.config.Configuration;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: angelsl
 * Date: 2/6/11
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScriptPlugin implements Plugin, Listener {

    static Logger l = Logger.getLogger("Minecraft.JxplPlugin");

    private boolean isEnabled = false;
    private final PluginLoader loader;
    private final Server server;
    private final File file;
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private Invocable sEngine;
    private PluginHelper helper;
    private final HashMap<Event.Type, String> eventHandlers = new HashMap<Event.Type, String>();

    private static String getOrDefault(ScriptEngine e, String vname, String efault) {
        Object r = e.get(vname);
        return r == null || !(r instanceof String) ? efault : (String) r;
    }

    public ScriptPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ScriptEngine engine) {
        loader = pluginLoader;
        server = instance;
        file = plugin;
        description = desc;
        dataFolder = folder;
        helper = new PluginHelper();
        engine.put(getOrDefault(engine, "HELPER_VARIABLE_NAME", "helper"), helper);
        engine.put(getOrDefault(engine, "PLUGIN_VARIABLE_NAME", "plugin"), this);
        engine.put(getOrDefault(engine, "SERVER_VARIABLE_NAME", "server"), server);
        sEngine = (Invocable) engine;
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
        throw new RuntimeException("Script plugins do not have separate configuration files");
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
        tryInvoke("onDisable");
    }

    @Override
    public void onEnable() {
        isEnabled = true;
        tryInvoke("onEnable");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        return false;
    }

    public void onEvent(Event.Type type, Event args) {
        if (isEnabled && eventHandlers.containsKey(type)) {
            tryInvoke(eventHandlers.get(type), type, args);
        }
    }

    private Object tryInvoke(String funcName, Object... params) {
        try {
            return sEngine.invokeFunction(funcName, params);
        } catch (Throwable e) {
            l.log(Level.WARNING, "Error while running " + funcName + " of script " + file.getName() + ".", e);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public Invocable getScriptEngine() {
        return sEngine;
    }

    private class PluginHelper {
        /**
         * Register an event, specifying the function that should handle the event
         *
         * @param event        The type of the event that will trigger the function
         * @param priority     The priority of the event handler
         * @param functionName The name of the function that will handle the event
         */
        @SuppressWarnings("unused")
        public void registerEvent(Event.Type event, Event.Priority priority, String functionName) {
            ScriptPlugin.this.eventHandlers.put(event, functionName);
            ScriptPlugin.this.server.getPluginManager().registerEvent(event, ScriptPlugin.this, priority, ScriptPlugin.this);
        }

        /**
         * Logs a message.
         *
         * @param l       The level of the message
         * @param message The message to be logged
         */
        @SuppressWarnings("unused")
        public void log(Level l, String message) {
            ScriptPlugin.l.log(l, message);
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
        		} catch(Throwable t) {
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
         * @return The file's contents, or "" if an error occured or the file does not exist.
         */
        public String getFileContents(String path) {
            return getFileContents(new File(path));
        }

        /**
         * Gets the contents of a file.
         *
         * @param file The file describing the file whose contents are to be read
         * @return The file's contents, or "" if an error occured or the file does not exist.
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
                reader.close();
            } catch (Throwable e) {
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
