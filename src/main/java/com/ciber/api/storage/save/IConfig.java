package com.ciber.api.storage.save;

//import com.ciber.api.manager.Manager;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IConfig {

    boolean contains(String path);

    void save();

    File getFile();

    void setFile(File file);

    Map<String, Object> getValues();

    void setValues(Map<String, Object> values);

    <T>void set(String path, T def);

    <T> T get(String path);

    <T> T get(String path, Class<T> aClass);

    <T> SimpleManager<T> getManager(String path, Class<T> aClass);

    SimpleManager<?> getManager(String path);

    String getString(String path);

    String getString(String path, String def);

    short getShort(String path);

    short getShort(String path, short def);

    long getLong(String path);

    long getLong(String path, long def);

    int getInt(String path);

    int getInt(String path, int def);

    boolean getBoolean(String path, boolean def);

    boolean getBoolean(String path);

    List<String> getStringList(String path);

    List<Integer> getIntList(String path);

    <T> List<T> getList(String path, Class<T> tList);

    Map<String, Object> getMap(String path);

    ConfigSection getSection(String path);

    ConfigSection getRoot();

    static IConfig createConfig(String path) {
        return (IConfig) new Config(path);
    }
}
