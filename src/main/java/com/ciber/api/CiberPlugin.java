package com.ciber.api;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class CiberPlugin extends JavaPlugin {

    private List<Class<?>> getClasses(String pluginPackage, Class<?> registerClasses) {
        List<Class<?>> listClasses = new ArrayList<Class<?>>();
        CodeSource source = registerClasses.getProtectionDomain().getCodeSource();
        if (source != null) {
            URL resource = source.getLocation();
            String resourcePath = resource.getPath().replace("%20", " ");
            String jarFilePath = resourcePath.replaceFirst(
                    "[.]jar[!].*", ".jar").replaceFirst("file:", "");
            String realPath = pluginPackage.replace(".", "/");
            try {
                Enumeration<JarEntry> entries = new JarFile(new File(jarFilePath)).entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class") && name.startsWith(realPath) && !name.contains("$")) {
                        String className = name.replace("/", ".").replace(
                                "\\", ".").replace(".class", "");
                        try {
                            listClasses.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            getLogger().log(Level.SEVERE, "Nao foi possivel carregar" + className, e);
                        }
                    }
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Nao foi possivel encontrar " + jarFilePath, e);
            }
        }
        return listClasses;
    }

    public void registerCommands(String commandPackage) {
        try {
            Server server = getServer();
            Class<?> serverClass = server.getClass();
            Field field = serverClass.getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(getServer());
            getClasses(this.getClass().getPackage().getName(), this.getClass()).forEach(commandClass -> {
                try {
                    Constructor<?> constructor = commandClass.getConstructor(this.getClass());
                    constructor.setAccessible(true);
                    Object instance = constructor.newInstance(this);
                    if(instance instanceof CiberCommand) {
                        CiberCommand command = (CiberCommand) instance;
                        map.register(this.getName(), command);
                    }
                }catch (NoSuchMethodException e) {
                    try {
                        Constructor<?> constructor = commandClass.getConstructor();
                        constructor.setAccessible(true);
                        Object instance = constructor.newInstance();
                        if (instance instanceof CiberCommand) {
                            CiberCommand command = (CiberCommand) instance;
                            map.register(this.getName(), command);
                        }
                    }catch (Exception ignored) {
                    }
                }catch (Exception e) {
                    getLogger().log(Level.SEVERE, "Ocorreu uma excecao inexperada.", e);
                }
            });
        }catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ocorreu uma excecao inesperada.", e);
        }
    }

    public void registerEvents(String eventPackage) {
        getClasses(this.getClass().getPackage().getName(), this.getClass()).forEach( eventClass -> {
            try {
                Constructor<?> constructor = eventClass.getConstructor(this.getClass());
                constructor.setAccessible(true);
                Object instance = constructor.newInstance(this);
                if(instance instanceof CiberEvent) {
                    CiberEvent event = (CiberEvent) instance;
                    getServer().getPluginManager().registerEvents(event, this);
                }
            }catch (NoSuchMethodException e) {
                try {
                    Constructor<?> constructor = eventClass.getConstructor();
                    constructor.setAccessible(true);
                    Object instance = constructor.newInstance();
                    if(instance instanceof CiberEvent) {
                        CiberEvent event = (CiberEvent) instance;
                        getServer().getPluginManager().registerEvents(event, this);
                    }
                }catch (Exception ignored){
                }
            }catch (Exception e) {
                getLogger().log(Level.SEVERE, "Ocorreu uma excecao inesperada.", e);
            }
        });
    }

    public void msg(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }
}
