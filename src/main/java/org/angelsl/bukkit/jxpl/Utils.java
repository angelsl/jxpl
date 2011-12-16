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

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import sun.org.mozilla.javascript.internal.NativeObject;

import javax.script.ScriptEngine;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// I know it's bad practice to have a class containing miscellaneous methods but I really cba. so fuck practice.
class Utils {
    private static Logger l = Logger.getLogger("Minecraft.JxplPlugin");
    public static String removeExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1) {
            return filename;
        }

        return filename.substring(0, extensionIndex);
    }

    public static String removeFromDot(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.indexOf(".");
        if (extensionIndex == -1) {
            return filename;
        }

        return filename.substring(0, extensionIndex);
    }

    public static String getStringOrDefault(ScriptEngine e, String vname, String efault) {
        Object r = e.get(vname);
        return r == null || !(r instanceof String) ? efault : (String) r;
    }

    public static String getStringOrExcept(ScriptEngine e, String vname) {
        Object r = e.get(vname);
        if (r == null || !(r instanceof String)) {
            throw new IllegalArgumentException("No variable named " + vname + " in script engine that is a String");
        }
        return (String) r;
    }

    public static Object getOrExcept(ScriptEngine e, String vname) {
        Object r = e.get(vname);
        if (r == null) {
            throw new IllegalArgumentException("No variable named " + vname + " in script engine");
        }
        return r;
    }

    public static String getOrDefault(Map<String, Object> e, String key, String efault) {
        Object r = e.get(key);
        return r == null || !(r instanceof String) ? efault : (String) r;
    }

    public static Boolean getOrDefault(Map<String, Object> e, String key, Boolean efault) {
        Object r = e.get(key);
        return r == null || !(r instanceof Boolean) ? efault : (Boolean) r;
    }

    public static PluginDescriptionFile getPdfFromMap(Map<String, Object> tmap) throws InvalidDescriptionException {
        Map<String, Object> map = new HashMap<String, Object>(tmap);
        if (!map.containsKey("main")) {
            map.put("main", "");
        }
        try {
            PluginDescriptionFile pdf = new PluginDescriptionFile("MISSINGNO.", "MISSINGNO.", "MISSINGNO.");
            Method loadMap = PluginDescriptionFile.class.getDeclaredMethod("loadMap", Map.class);
            loadMap.setAccessible(true);
            loadMap.invoke(pdf, map);
            return pdf;
        } catch (Throwable t) {
            throw new InvalidDescriptionException(t, "Failed to create plugin description file.");
        }
    }

    public static Object getFieldHelper(Object o, String name) {
        try {
            Class c = o.getClass();
            Field f = null;
            while (f == null) {
                if (c != null) {
                    try {
                        f = c.getDeclaredField(name);
                    } catch (Throwable t) {
                        c = c.getSuperclass();
                    }
                }
            }
            if (f != null) {
                f.setAccessible(true);
                return f.get(o);
            }
        } catch (Throwable t) {
        }

        return null;
    }

    public static boolean setFieldHelper(Object o, String name, Object v) {
        try {
            Class c = o.getClass();
            Field f = null;
            while (f == null) {
                if (c != null) {
                    try {
                        f = c.getDeclaredField(name);
                    } catch (Throwable t) {
                        c = c.getSuperclass();
                    }
                }
            }
            if (f != null) {
                f.setAccessible(true);
                f.set(o, v);
                return true;
            }
        } catch (Throwable t) {
        }
        return false;
    }

    public static boolean callMethodHelper(Object toHack, String methodName, Object... params) {
        Class[] paramClasses = new Class[params.length];
        for (int i = 0; i < paramClasses.length; ++i)
            paramClasses[i] = params[i].getClass();
        Class toHackClass = toHack.getClass();
        for (; ; ) {
            try {
                Method loadFromMap = toHackClass.getDeclaredMethod(methodName, paramClasses);
                loadFromMap.setAccessible(true);
                loadFromMap.invoke(toHack, params);
                return true;
            } catch (NoSuchMethodException nsme) {
                toHackClass = toHackClass.getSuperclass();
                if(toHackClass == null) return false;
                continue;
            }  catch(Throwable ite)
            {
                log(Level.SEVERE, String.format("Failed to call method \"%s\" of class \"%s\" or superclasses by reflection", methodName, toHack.getClass().getName()), ite);
                return false;
            }
        }
    }

    public static boolean dirExistOrCreate(File dir) {
        if (!dir.exists() || (dir.exists() && dir.isFile())) {
            if (dir.exists() && dir.isFile()) {
                dir.delete();
                Utils.log(Level.INFO, String.format("Deleting file \"%s\".", dir.getAbsolutePath()));
            }

            if (!dir.mkdirs()) {
                Utils.log(Level.SEVERE, String.format("Failed to create directory \"%s\"!", dir.getAbsolutePath()));
                return false;
            } else Utils.log(Level.INFO, String.format("Created directory \"%s\".", dir.getAbsolutePath()));
        }
        return true;
    }
    
    public static void log(Level level, String message)
    {
        l.log(level, String.format("[jxpl] %s", message));
    }

    public static void log(Level level, String message, Throwable t)
    {
        l.log(level, String.format("[jxpl] %s", message), t);
    }

    public static String join(List<String> list, String delim) {

        StringBuilder sb = new StringBuilder();

        String loopDelim = "";

        for(String s : list) {

            sb.append(loopDelim);
            sb.append(s);

            loopDelim = delim;
        }

        return sb.toString();
    }
    
    public static Map<String, Object> scriptObjectToMap(NativeObject no)
    {
        HashMap<String, Object> ret = new HashMap<String, Object>();
        for(Object id : no.getAllIds())
        {
            if(id instanceof Integer) {
                Integer cid = (Integer)id;
                Object cido = no.get(cid, no);
                ret.put(cid.toString(), cido instanceof NativeObject ? scriptObjectToMap((NativeObject)cido) : cido);
            } else if(id instanceof String) {
                String cid = (String)id;
                Object cido = no.get(cid, no);
                ret.put(cid, cido instanceof NativeObject ? scriptObjectToMap((NativeObject)cido) : cido);
            }
        }
        return ret;
    }

}
