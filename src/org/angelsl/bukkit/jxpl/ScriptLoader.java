package org.angelsl.bukkit.jxpl;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.*;

import javax.script.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ScriptLoader implements PluginLoader {
    static Logger l = Logger.getLogger("Minecraft.JxplPlugin");

    private final Server instance;
    private Pattern[] fileFilters;
    private final ScriptEngineManager manager = new ScriptEngineManager();

    public ScriptLoader(Server instance) {
        this.instance = instance;

        ArrayList<Pattern> fileFiltersR = new ArrayList<Pattern>();
        for (ScriptEngineFactory sef : manager.getEngineFactories()) {
            try {
                Invocable t = ((Invocable) sef.getScriptEngine());
            } catch (Throwable t) {
                // engine does not support invocable. pass.
                continue;
            }
            for (String ext : sef.getExtensions()) {
                l.log(Level.INFO, "Adding file extension \"." + ext + "\" for scripting engine \"" + sef.getEngineName() + "\".");
                fileFiltersR.add(Pattern.compile("[^.].*" + Pattern.quote("." + ext) + "$"));
            }
        }
        fileFilters = fileFiltersR.toArray(new Pattern[0]);
    }

    public Plugin loadPlugin(File file, boolean ignoreSoftDependencies) throws InvalidPluginException, InvalidDescriptionException {
        return loadPlugin(file);
    }

    public Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException {
        if (!file.getParentFile().equals(JxplPlugin.getScriptsDir())) return null;
        ScriptEngine se = getScriptEngine(file);
        ScriptPlugin sp = new ScriptPlugin(this, instance, new PluginDescriptionFile((String) se.get("scriptName"), (String) se.get("scriptVersion"), "NOT APPLICABLE YOU BUKKIT RETARDS"), getDataFolder(file), file, se);
        JxplPlugin.getLoadedPlugins().add(sp);
        l.log(Level.INFO, "Loaded script " + file.getName());
        return sp;
    }

    protected ScriptEngine getScriptEngine(File f) {
        ScriptEngine se = manager.getEngineByExtension(f.getName().substring(f.getName().lastIndexOf(".") + 1));
        FileInputStream is = null;
        InputStreamReader isr = null;
        try {
            is = new FileInputStream(f);
            isr = new InputStreamReader(is);
            se.eval(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            l.log(Level.WARNING, "File not found while loading script!", e);
            return null;
        } catch (ScriptException e) {
            e.printStackTrace();
            l.log(Level.WARNING, "Error while evaluating script!", e);
            return null;
        } finally {
            try {
                isr.close();
                is.close();
            } catch (Throwable t) {
            } // fuck off
        }
        return se;
    }

    private File getDataFolder(File file) {
        File dataFolder = null;

        String filename = file.getName();
        int index = file.getName().lastIndexOf(".");

        if (index != -1) {
            String name = filename.substring(0, index);
            dataFolder = new File(file.getParentFile(), name);
        } else {
            // This is if there is no extension, which should not happen
            // Using _ to prevent name collision
            dataFolder = new File(file.getParentFile(), filename + "_");
        }

        //dataFolder.mkdirs();

        return dataFolder;
    }

    public Pattern[] getPluginFileFilters() {
        return fileFilters;
    }

    public EventExecutor createExecutor(final Event.Type type, Listener listener) {
        return new EventExecutor() {
            public void execute(Listener listener, Event event) {
                ((ScriptPlugin.ScriptEventListener) listener).onEvent(type, event);
            }
        };
    }

    public void enablePlugin(Plugin plugin) {
        if (!(plugin instanceof ScriptPlugin))
            throw new RuntimeException("Wrong PluginLoader called to enable plugin!");
        instance.getPluginManager().callEvent(new PluginEvent(Event.Type.PLUGIN_ENABLE, plugin));
        plugin.onEnable();
    }

    public void disablePlugin(Plugin plugin) {
        if (!(plugin instanceof ScriptPlugin))
            throw new RuntimeException("Wrong PluginLoader called to enable plugin!");
        instance.getPluginManager().callEvent(new PluginEvent(Event.Type.PLUGIN_DISABLE, plugin));
        plugin.onDisable();
    }
}
