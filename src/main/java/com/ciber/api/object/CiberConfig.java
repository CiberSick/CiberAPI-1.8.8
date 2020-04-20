package com.ciber.api.object;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class CiberConfig {

    private Plugin plugin;
    private String name;
    private YamlConfiguration config;
    private File file;

    public CiberConfig(Plugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        reload();
    }

    private void reload() {
        file = new File(plugin.getDataFolder(), name);
        config = YamlConfiguration.loadConfiguration(file);

        if (file.exists()) {
            save();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean exist() {
        if (file.exists()) return true;
        return false;
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public void setString(String path, String value) {
        config.set(path, value);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public void setInt(String path, int value) {
        config.set(path, value);
    }

    public double getDouble(double path) {
        return config.getDouble(String.valueOf(path));
    }

    public void setDouble(String path, double value) {
        config.set(path, value);
    }

    public long getLong(long path) {
        return config.getLong(String.valueOf(path));
    }

    public void setLong(String path, long value) {
        config.set(path, value);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public void setBoolean(String path, boolean value) {
        config.set(path, value);
    }

    public final Object get(String path) {
        return config.get(path);
    }

    public final void set(String path, Object value) {
        config.set(path, value);
    }

    public final Boolean contains(String path) {
        if(get(path) != null) return true;
        return false;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public void setConfig(YamlConfiguration config) {
        this.config = config;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}