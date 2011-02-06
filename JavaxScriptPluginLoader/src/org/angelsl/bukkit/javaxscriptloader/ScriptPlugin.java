package org.angelsl.bukkit.javaxscriptloader;

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
import javax.script.ScriptException;
import java.io.File;
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

    static Logger l = Logger.getLogger("Minecraft.ScriptLoaderPlugin");

    private boolean isEnabled = false;
    private final PluginLoader loader;
    private final Server server;
    private final File file;
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private Invocable sEngine;
    private final Configuration config;
    private final HashMap<Event.Type, String> eventHandlers = new HashMap<Event.Type, String>();


    public ScriptPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ScriptEngine engine) {
        loader = pluginLoader;
        server = instance;
        file = plugin;
        description = desc;
        dataFolder = folder;
        engine.put("plugin", this);
        sEngine = (Invocable)engine;
        config = new Configuration(new File(dataFolder, "config.yml"));
        config.load();
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
        return config;
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

    private Object tryInvoke(String funcName, Object... params)
    {
        try {
            return sEngine.invokeFunction(funcName, params);
        } catch (Throwable e) {
            l.log(Level.WARNING, "Error while running "+ funcName+" of script " + file.getName() + ".", e);
        }
        return null;
    }

    @Override
    public void onEnable() {
        isEnabled = true;
        tryInvoke("onEnable");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(!isEnabled) return false;
        return (Boolean) tryInvoke("onCommand", sender, command, commandLabel, args);
    }

    public void handleEvent(Event.Type type, Event args)
    {
        if(isEnabled && eventHandlers.containsKey(type))
        {
            tryInvoke(eventHandlers.get(type), type, args);
        }
    }

    public void registerEvent(Event.Type event, Event.Priority priority, String functionName)
    {
        eventHandlers.put(event, functionName);
        server.getPluginManager().registerEvent(event, this, priority, this);
    }
    
    public void log(Level l, String message)
    {
        ScriptPlugin.l.log(l, message);
    }
}
