package com.mysticx.bukkit.backupplugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class Configuration {

    private Properties properties;
    private String name;

    public Configuration(String name) {
        this.properties = new Properties();
        this.name = name;

        File file = new File(name);

        if (file.exists())
            load();
        else
            save();
    }

    public void load() {
        try {
            this.properties.load(new FileInputStream(this.name));
        } catch (IOException ioex) {
            MessageHandler.log(Level.SEVERE, "Can't load config!", ioex);
        }
    }

    public void save() {
        try {
            this.properties.store(new FileOutputStream(this.name),
                    "BackupPlugin Config File");
        } catch (IOException ioex) {
            MessageHandler.log(Level.SEVERE, "Can't save config!", ioex);
        }
    }

    public Map<String, String> returnMap() throws FileNotFoundException, IOException {
        Map<String, String> map = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(
                new FileReader(this.name));
        String line;
        while ((line = reader.readLine()) != null) {
            if ((line.trim().length() == 0) || (line.charAt(0) == '#')) {
                continue;
            }
            int delimPosition = line.indexOf('=');
            String key = line.substring(0, delimPosition).trim();
            String value = line.substring(delimPosition + 1).trim();
            map.put(key, value);
        }
        reader.close();
        return map;
    }

    public void removeKey(String key) {
        this.properties.remove(key);
        save();
    }

    public boolean keyExists(String key) {
        return this.properties.containsKey(key);
    }

    public String getString(String key) {
        if (this.properties.containsKey(key)) {
            return this.properties.getProperty(key);
        }

        return "";
    }

    public String getString(String key, String value) {
        if (this.properties.containsKey(key)) {
            return this.properties.getProperty(key);
        }
        setString(key, value);
        return value;
    }

    public void setString(String key, String value) {
        this.properties.setProperty(key, value);
        save();
    }

    public int getInt(String key) {
        if (this.properties.containsKey(key)) {
            return Integer.parseInt(this.properties.getProperty(key));
        }

        return 0;
    }

    public int getInt(String key, int value) {
        if (this.properties.containsKey(key)) {
            return Integer.parseInt(this.properties.getProperty(key));
        }

        setInt(key, value);
        return value;
    }

    public void setInt(String key, int value) {
        this.properties.setProperty(key, String.valueOf(value));
        save();
    }

    public double getDouble(String key) {
        if (this.properties.containsKey(key)) {
            return Double.parseDouble(this.properties.getProperty(key));
        }

        return 0.0D;
    }

    public double getDouble(String key, double value) {
        if (this.properties.containsKey(key)) {
            return Double.parseDouble(this.properties.getProperty(key));
        }

        setDouble(key, value);
        return value;
    }

    public void setDouble(String key, double value) {
        this.properties.setProperty(key, String.valueOf(value));
        save();
    }

    public long getLong(String key) {
        if (this.properties.containsKey(key)) {
            return Long.parseLong(this.properties.getProperty(key));
        }

        return 0L;
    }

    public long getLong(String key, long value) {
        if (this.properties.containsKey(key)) {
            return Long.parseLong(this.properties.getProperty(key));
        }

        setLong(key, value);
        return value;
    }

    public void setLong(String key, long value) {
        this.properties.setProperty(key, String.valueOf(value));
        save();
    }

    public boolean getBoolean(String key) {
        if (this.properties.containsKey(key)) {
            return Boolean.parseBoolean(this.properties.getProperty(key));
        }

        return false;
    }

    public boolean getBoolean(String key, boolean value) {
        if (this.properties.containsKey(key)) {
            return Boolean.parseBoolean(this.properties.getProperty(key));
        }

        setBoolean(key, value);
        return value;
    }

    public void setBoolean(String key, boolean value) {
        this.properties.setProperty(key, String.valueOf(value));
        save();
    }
}
