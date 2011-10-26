package org.angelsl.bukkit.jxpl;

import javax.script.ScriptEngine;

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
        if (extensionIndex == -1)
            return filename;

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
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }

    public static String getStringOrDefault(ScriptEngine e, String vname, String efault) {
        Object r = e.get(vname);
        return r == null || !(r instanceof String) ? efault : (String) r;
    }

    public static String getStringOrExcept(ScriptEngine e, String vname) {
        Object r = e.get(vname);
        if (r == null || !(r instanceof String))
            throw new IllegalArgumentException("No variable named " + vname + " in script engine that is a String");
        return (String) r;
    }

    public static Object getOrExcept(ScriptEngine e, String vname) {
        Object r = e.get(vname);
        if (r == null)
            throw new IllegalArgumentException("No variable named " + vname + " in script engine");
        return r;
    }

}
