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

import javax.script.ScriptEngine;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class Utils {
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

}
