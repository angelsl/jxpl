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

import org.angelsl.bukkit.jxpl.rhino.RhinoScriptEngineFactory;
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
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptLoader implements PluginLoader {
    private final Server instance;
    private final HashMap<Pattern, ScriptEngineFactory> factoryAssociation = new HashMap<Pattern, ScriptEngineFactory>();
    private final ScriptEngineManager manager;
    private final List<String> BLACKLISTED = Arrays.asList(new String[]{"apple.applescript.AppleScriptEngineFactory", "com.sun.script.javascript.RhinoScriptEngineFactory"});

    public ScriptLoader(Server instance) {
        this.instance = instance;
        injectScriptEngines();
        manager = new ScriptEngineManager();
        HashMap<String, ScriptEngineFactory> fassocR = new HashMap<String, ScriptEngineFactory>();
        for (ScriptEngineFactory sef : manager.getEngineFactories()) {
            if(BLACKLISTED.contains(sef.getClass().getName())) {
                Utils.log(Level.INFO, String.format("Not using script engine \"%s %s\", factory \"%s\"; blacklisted", sef.getEngineName(), sef.getEngineVersion(), sef.getClass().getName()));
                continue;
            }
            addScriptEngineHelper(fassocR, sef);
        }
        addScriptEngineHelper(fassocR, new RhinoScriptEngineFactory());
        for(Map.Entry<String, ScriptEngineFactory> messef : fassocR.entrySet())
        {
            factoryAssociation.put(Pattern.compile(messef.getKey()), messef.getValue());
        }
    }
    
    private void addScriptEngineHelper(HashMap<String, ScriptEngineFactory> fassocR, ScriptEngineFactory sef)
    {
        try {
            ScriptEngine se = sef.getScriptEngine();
            Invocable toss = ((Invocable)se);
        } catch (ClassCastException cce) {
            Utils.log(Level.INFO, String.format("Not using script engine \"%s %s\", factory \"%s\"; not Invocable", sef.getEngineName(), sef.getEngineVersion(), sef.getClass().getName()));
            return;
        } catch (Throwable t) {
            Utils.log(Level.SEVERE, String.format("Error while checking script engine \"%s %s\"!", sef.getEngineName(), sef.getEngineVersion()), t);
            return;
        }
        for (String ext : sef.getExtensions()) {
            String ptrn = ("[^.].*" + Pattern.quote("." + ext.toLowerCase()) + "$");
            if(fassocR.containsKey(ptrn))
            {
                Utils.log(Level.WARNING, String.format("File extension \"%s\" has more than one script engine handling; will use first loaded engine.", ext));
                Utils.log(Level.WARNING, String.format("Not adding file extension \".%s\" for script engine \"%s %s\".", ext, sef.getEngineName(), sef.getEngineVersion()));//"Adding file extension \"." + ext + "\" for scripting engine \"" + sef.getEngineName() + "\".");
                continue;
            }
            fassocR.put(ptrn, sef);
            Utils.log(Level.INFO, String.format("File extension \".%s\" will be handled by script engine \"%s %s\".", ext, sef.getEngineName(), sef.getEngineVersion()));//"Adding file extension \"." + ext + "\" for scripting engine \"" + sef.getEngineName() + "\".");
        }
    }

    private static void injectScriptEngines() {
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
    
    private ScriptEngineFactory getScriptEngineFactory(String fname) throws InvalidPluginException
    {
        for(Map.Entry<Pattern, ScriptEngineFactory> sef : factoryAssociation.entrySet())
        {
            Matcher m = sef.getKey().matcher(fname);
            if(m.find()) return sef.getValue();
        }
        return null;
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
            Utils.log(Level.SEVERE, String.format("Not loading script \"%s\"; SCRIPT_PDF undefined.", file.getName()), iae);
            throw new InvalidDescriptionException(iae, "SCRIPT_PDF undefined");
        } catch (ClassCastException cce) {
            Utils.log(Level.SEVERE, String.format("Not loading script \"%s\"; SCRIPT_PDF not of type Map<String, Object>.", file.getName()), cce);
            throw new InvalidDescriptionException(cce, "SCRIPT_PDF not of type Map<String, Object>");
        } catch (FileNotFoundException fnfe) {
            Utils.log(Level.SEVERE, String.format("Not loading script \"%s\"; file not found.", file.getName()), fnfe);
            throw new InvalidPluginException(fnfe);
        } catch (ScriptException se) {
            Utils.log(Level.SEVERE, String.format("Not loading script \"%s\"; error while parsing script.", file.getName()), se);
            throw new InvalidPluginException(se);
        }
    }

    protected ScriptEngine getScriptEngine(File f) throws FileNotFoundException, ScriptException, InvalidPluginException {
        ScriptEngineFactory sef = getScriptEngineFactory(f.getName());
        if(sef == null)
        {
            Utils.log(Level.INFO, String.format("Refusing to load plugin \"%s\"; extension not handled by jxpl", f.getName()));
            throw new InvalidPluginException(new IllegalArgumentException(String.format("Not loading plugin \"%s\"; jxpl doesn't handle this extension!", f.getName())));
        }
        ScriptEngine se = sef.getScriptEngine();
        FileInputStream is = null;
        InputStreamReader isr = null;
        try {
            is = new FileInputStream(f);
            isr = new InputStreamReader(is);
            se.put(ScriptEngine.FILENAME, f.getName());
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
        return factoryAssociation.keySet().toArray(new Pattern[0]);
    }

    public EventExecutor createExecutor(final Event.Type type, Listener listener) {
        return new EventExecutor() {
            public void execute(Listener listener, Event event) {
                ((ScriptPlugin.ScriptEventListener) listener).onEvent(type, event);
            }
        };
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        throw new UnsupportedOperationException("Script plugins do not have separate listener classes.");
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
