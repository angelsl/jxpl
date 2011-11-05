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

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
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
        try {
            if (!file.getParentFile().equals(JxplPlugin.getScriptsDir())) {
                return null;
            }
            ScriptEngine se = getScriptEngine(file);
            ScriptPlugin sp = new ScriptPlugin(this, instance, file, se);
            JxplPlugin.getLoadedPlugins().add(sp);
            l.log(Level.INFO, "Loaded script " + file.getName());
            return sp;
        } catch (IllegalArgumentException iae) {
            l.log(Level.SEVERE, String.format("Not loading script \"%s\"; SCRIPT_PDF undefined.", file.getName()));
            throw new InvalidDescriptionException(iae, "SCRIPT_PDF undefined");
        } catch (ClassCastException cce) {
            l.log(Level.SEVERE, String.format("Not loading script \"%s\"; SCRIPT_PDF not of type Map<String, Object>.", file.getName()));
            throw new InvalidDescriptionException(cce, "SCRIPT_PDF not of type Map<String, Object>");
        }
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
        if (!(plugin instanceof ScriptPlugin)) {
            throw new RuntimeException("Wrong PluginLoader called to enable plugin!");
        }
        instance.getPluginManager().callEvent(new PluginEnableEvent(plugin));
        plugin.onEnable();
    }

    public void disablePlugin(Plugin plugin) {
        if (!(plugin instanceof ScriptPlugin)) {
            throw new RuntimeException("Wrong PluginLoader called to enable plugin!");
        }
        instance.getPluginManager().callEvent(new PluginDisableEvent(plugin));
        plugin.onDisable();
    }
}
