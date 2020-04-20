package com.ciber.api.storage.save;

import com.google.common.collect.Maps;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigSection implements IConfig {

    @Override
    public String toString() {
        String name = "NULL";
        if (root != null) {
            name = root.name;
        }
        return String.format("ConfigSection(root=%s, api=%s,  name='%s')", name, api, this.name);
    }

    protected ConfigSection root = null;
    protected Map<String, Object> values;
    protected SavableAPI.ISavableAPI api = SavableAPI.getInstance();
    protected List<ConfigSection> sections;
    protected String name;

    @Override
    public boolean contains(String path) {
        return values.containsKey(path);
    }

    @Override
    public void save() {
        try {
            throw new OperationNotSupportedException("Can not save only the ConfigSection");
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getFile() {
        try {
            throw new OperationNotSupportedException("Can not get file of a ConfigSection");
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setFile(File file) {
        try {
            throw new OperationNotSupportedException("Can not set file of a ConfigSection");
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    protected ConfigSection getSection(String name, ConfigSection sec, Map<String, Object> map) {
        sec.values = map;
        sec.name = name;
        try {
            sec.values.forEach((key, value) -> {
                if (value instanceof Map) {
                    ConfigSection sub = new ConfigSection((Map<String, Object>) value, sec, key);
                    sec.sections.add(getSection(key, sub, sub.values));
                }
            });
        } catch (Exception ex) {
            System.out.println("Error on load config section sub sections");
            return null;
        }
        return sec;
    }

    ConfigSection() {
        values = Maps.newLinkedHashMap();
        name = "";
        sections = new ArrayList<>();
    }

    ConfigSection(Map<String, Object> values, ConfigSection root, String name) {
        this.values = values;
        this.root = root;
        this.name = name;
        sections = new ArrayList<>();
    }

    @Override
    public Map<String, Object> getValues() {
        return values;
    }

    @Override
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public <T> void set(String path, T def) {
        if (!values.containsKey(path)) {
            values.put(path, api.save(def, false));
        } else {
            values.remove(path);
            set(path, def);
        }
    }

    @Override
    public <T> T get(String path) {
        return get(path, null);
    }

    @Override
    public <T> T get(String path, Class<T> aClass) {
        return api.pull(values.get(path), aClass, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> SimpleManager<T> getManager(String path, Class<T> aClass) {
        return (SimpleManager<T>) get(path, aClass);
    }

    @Override
    public SimpleManager<?> getManager(String path) {
        return getManager(path, SimpleManager.class);
    }

    @Override
    public String getString(String path) {
        return get(path, String.class);
    }

    @Override
    public String getString(String path, String def) {
        return get(path, String.class) != null ? get(path, String.class) : def;
    }

    @Override
    public short getShort(String path) {
        return get(path, short.class);
    }

    @Override
    public short getShort(String path, short def) {
        return get(path, short.class) != null ? get(path, short.class) : def;
    }

    @Override
    public long getLong(String path) {
        return get(path, long.class);
    }

    @Override
    public long getLong(String path, long def) {
        return get(path, long.class) != null ? get(path, long.class) : def;
    }

    @Override
    public int getInt(String path) {
        return get(path, int.class);
    }

    @Override
    public int getInt(String path, int def) {
        return get(path, int.class) != null ? get(path, int.class) : def;
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return get(path, boolean.class) != null ? get(path, boolean.class) : def;
    }

    @Override
    public boolean getBoolean(String path) {
        return get(path, boolean.class);
    }

    @Override
    public List<String> getStringList(String path) {
        return getList(path, String.class);
    }

    @Override
    public List<Integer> getIntList(String path) {
        return getList(path, int.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getList(String path, Class<T> tList) {
        return (List<T>) get(path, tList);
    }

    @Override
    public Map<String, Object> getMap(String path) {
        return getSection(path).values;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public ConfigSection getSection(String name) {
        return sections.stream().filter(it -> it.name.contentEquals(name)).findFirst().get();
    }

    @Override
    public ConfigSection getRoot() {
        return root;
    }

    public void setRoot(ConfigSection root) {
        this.root = root;
    }

    public SavableAPI.ISavableAPI getApi() {
        return api;
    }

    public void setApi(SavableAPI.ISavableAPI api) {
        this.api = api;
    }

    public List<ConfigSection> getSections() {
        return sections;
    }

    public void setSections(List<ConfigSection> sections) {
        this.sections = sections;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
