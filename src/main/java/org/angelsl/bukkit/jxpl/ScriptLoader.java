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
import java.io.*;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ScriptLoader implements PluginLoader {
    private final Server instance;
    private Pattern[] fileFilters;
    private final ScriptEngineManager manager;

    public ScriptLoader(Server instance) {
        this.instance = instance;
        loadScriptEngines();
        manager = new ScriptEngineManager();
        ArrayList<Pattern> fileFiltersR = new ArrayList<Pattern>();
        for (ScriptEngineFactory sef : manager.getEngineFactories()) {
            try {
                Invocable t = ((Invocable) sef.getScriptEngine());
            } catch (Throwable t) {
                // engine does not support invocable. pass.
                Utils.log(Level.SEVERE, String.format("Failed to load script engine \"%s %s\"! Is the engine Invocable?", sef.getEngineName(), sef.getEngineVersion()), t);
                continue;
            }
            for (String ext : sef.getExtensions()) {
                Utils.log(Level.INFO, "Adding file extension \"." + ext + "\" for scripting engine \"" + sef.getEngineName() + "\".");
                fileFiltersR.add(Pattern.compile("[^.].*" + Pattern.quote("." + ext) + "$"));
            }
        }
        fileFilters = fileFiltersR.toArray(new Pattern[0]);
    }

    private static void loadScriptEngines() {
        ClassLoader ucl = Thread.currentThread().getContextClassLoader();
        if (!(ucl instanceof URLClassLoader)) {
            Utils.log(Level.WARNING, String.format("Thread classloader is not a URLClassLoader but a \"%s\"! Refusing to inject script engine JARs.", ucl.getClass().getName()));
            return;
        }
        File libDir = new File(JxplPlugin.getPlugin().getDataFolder(), "lib");
        if (!Utils.dirExistOrCreate(libDir)) {
            Utils.log(Level.SEVERE, String.format("jxpl lib directory doesn't exist and creation failed; refusing to inject script engine JARs."));
            return;
        }
        File[] engineJars = libDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });

        for (File jar : engineJars) {
            try {
                Utils.callMethodHelper(ucl, "addURL", jar.toURI().toURL());
                Utils.log(Level.INFO, String.format("Injected JAR at \"%s\".", jar.getAbsolutePath()));
            } catch (MalformedURLException murle) {
                Utils.log(Level.SEVERE, String.format("Failed to inject JAR at \"%s\"!", jar.getAbsolutePath()), murle);
                continue;
            }
        }
    }

    public Plugin loadPlugin(File file, boolean ignoreSoftDependencies) throws InvalidPluginException, InvalidDescriptionException {
        return loadPlugin(file);
    }

    public Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException {
        try {
            if (!file.getParentFile().equals(JxplPlugin.getScriptsDir())) {
                Utils.log(Level.SEVERE, String.format("Not loading script \"%s\"; script not in scripts directory.", file.getName()));
                throw new InvalidPluginException(new IllegalArgumentException("Script not in scripts directory."));
            }
            ScriptEngine se = getScriptEngine(file);

            ScriptPlugin sp = new ScriptPlugin(this, instance, file, se);
            JxplPlugin.getLoadedPlugins().add(sp);
            Utils.log(Level.INFO, String.format("Loaded script \"%s\" ([%s] version [%s] by [%s])", file.getName(), sp.getDescription().getName(), sp.getDescription().getVersion(), Utils.join(sp.getDescription().getAuthors(), ", ")));
            return sp;
        } catch (IllegalArgumentException iae) {
            Utils.log(Level.SEVERE, String.format("Not loading script \"%s\"; SCRIPT_PDF undefined.", file.getName()));
            throw new InvalidDescriptionException(iae, "SCRIPT_PDF undefined");
        } catch (ClassCastException cce) {
            Utils.log(Level.SEVERE, String.format("Not loading script \"%s\"; SCRIPT_PDF not of type Map<String, Object>.", file.getName()));
            throw new InvalidDescriptionException(cce, "SCRIPT_PDF not of type Map<String, Object>");
        } catch (FileNotFoundException fnfe) {
            Utils.log(Level.SEVERE, String.format("Not loading script \"%s\"; file not found.", file.getName()));
            throw new InvalidPluginException(fnfe);
        } catch (ScriptException se) {
            Utils.log(Level.SEVERE, String.format("Not loading script \"%s\"; error while parsing script.", file.getName()));
            throw new InvalidPluginException(se);
        }
    }

    protected ScriptEngine getScriptEngine(File f) throws FileNotFoundException, ScriptException {
        ScriptEngine se = manager.getEngineByExtension(f.getName().substring(f.getName().lastIndexOf(".") + 1));
        FileInputStream is = null;
        InputStreamReader isr = null;
        try {
            is = new FileInputStream(f);
            isr = new InputStreamReader(is);
            se.eval(isr);
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
            throw new IllegalArgumentException("Wrong PluginLoader called to enable plugin!");
        }
        instance.getPluginManager().callEvent(new PluginEnableEvent(plugin));
        plugin.onEnable();
    }

    public void disablePlugin(Plugin plugin) {
        if (!(plugin instanceof ScriptPlugin)) {
            throw new IllegalArgumentException("Wrong PluginLoader called to enable plugin!");
        }
        instance.getPluginManager().callEvent(new PluginDisableEvent(plugin));
        plugin.onDisable();
    }
}
