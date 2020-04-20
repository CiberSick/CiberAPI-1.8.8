package com.ciber.api.storage.save;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtils {

    private static final Logger logger = Logger.getLogger("Minecraft");

    public static boolean hasGetter(Class<?> aClass, Field property) {
        return getGetter(aClass, property, false) != null;
    }

    public static boolean hasSetter(Class<?> aClass, Field property) {
        return getSetter(aClass, property, false) != null;
    }

    private static Method getFieldMethod(Class<?> aClass, Field property, String prefix, boolean debug) {
        String name = property.getName();
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(prefix);
        boolean first = true;
        for (Character character : name.toCharArray()) {
            if (first) {
                nameBuilder.append(character.toString().toUpperCase());
                first = false;
                continue;
            }
            nameBuilder.append(character);
        }
        return getMethod(aClass, nameBuilder.toString(), debug);
    }

    public static Method getMethod(Class<?> aClass, String name, boolean debug, Class<?>... type) {
        try {
            return aClass.getDeclaredMethod(name, type);
        } catch (NoSuchMethodException ex) {
            if (debug) {
                logger.log(Level.SEVERE, String.format("No method %s found in %s.", name, aClass.getName()), ex);
            }
        }
        return null;
    }

    public static Method getGetter(Class<?> aClass, Field property, boolean debug) {
        if (property.isAnnotationPresent(SavableGetter.class)) {
            return getMethod(aClass, property.getAnnotation(SavableGetter.class).value(), debug);
        }
        return getFieldMethod(aClass, property, "get", debug);
    }

    public static Method getSetter(Class<?> aClass, Field property, boolean debug) {
        if (property.isAnnotationPresent(SavableSetter.class)) {
            return getMethod(aClass, property.getAnnotation(SavableSetter.class).value(), debug, Object.class);
        }
        return getFieldMethod(aClass, property, "set", debug);
    }

    public static Object get(Class<?> aClass, Field property, Object obj) {
        if (hasGetter(aClass, property)) {
            try {
                return getGetter(aClass, property, false).invoke(obj);
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE, String.format("%s's getter is private in %s", property.getName(), aClass.getName()), ex);
            } catch (InvocationTargetException ex) {
                logger.log(Level.SEVERE, String.format("Failed to call %s's getter in %s", property.getName(), aClass.getName()), ex);
            }
        } else {
            try {
                return property.get(obj);
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE, String.format("%s is private in %s", property.getName(), aClass.getName()), ex);
            }
        }
        return null;
    }

    public static void set(Class<?> aClass, Field property, Object obj, Object set) {
        if (hasSetter(aClass, property)) {
            try {
                getSetter(aClass, property, false).invoke(obj, set);
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE, String.format("%s's setter is private in %s", property.getName(), aClass.getName()), ex);
            } catch (InvocationTargetException ex) {
                logger.log(Level.SEVERE, String.format("Failed to call %s's setter in %s", property.getName(), aClass.getName()), ex);
            }
        } else {
            try {
                property.set(obj, set);
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE, String.format("%s is private in %s", property.getName(), aClass.getName()), ex);
            }
        }
    }

    public static List<Class<?>> getSuperClasses(Class<?> aClass) {
        List<Class<?>> classes = new ArrayList<>();
        Class<?> currentClass = aClass;
        while (currentClass != Object.class) {
            try {
                currentClass = currentClass.getSuperclass();
                classes.addAll(Arrays.asList(currentClass.getInterfaces()));
                classes.add(currentClass);
            } catch (Exception ex) {
                break;
            }
        }
        return classes;
    }

    public static List<Field> getProperties(Class<?> aClass, Predicate<? super Class<?>> predicate) {
        List<Field> properties = new ArrayList<>();
        for (Field f : aClass.getDeclaredFields()) {
            f.setAccessible(true);
            properties.add(f);
        }
        for (Class<?> cls1 : getSuperClasses(aClass)) {
            if (SavableAPI.getInstance().hasSavable(cls1)) {
                for (Field f : cls1.getDeclaredFields()) {
                    f.setAccessible(true);
                    properties.add(f);
                }
            }
        }
        return Collections.unmodifiableList(properties);
    }

    public static List<Field> getProperties(Class<?> aClass) {
        return getProperties(aClass, Objects::nonNull);
    }


    public static List<Class<?>> getClasses(String aPackage, Class<?> thisClass) {
        List<Class<?>> classes = new ArrayList<>();
        CodeSource codeSource = thisClass.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            URL codeSourceURL = codeSource.getLocation();
            String codeSourcePath = codeSourceURL.getPath().replace("%20", " ");
            String jarFilePath = codeSourcePath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
            String packageRelationPath = aPackage.replace('.', '/');
            try {
                JarFile jarFile = new JarFile(jarFilePath);
                Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry entry = jarEntryEnumeration.nextElement();
                    String jarEntryName = entry.getName();
                    if (jarEntryName.endsWith(".class") && jarEntryName.startsWith(packageRelationPath)) {
                        String classPath = jarEntryName.replace('/', '.')/*.replace('$', '.')*/.replace('\\', '.').replace(".class", "");
                        try {
                            Class<?> packageClass = Class.forName(classPath);
                            classes.add(packageClass);
                        } catch (ClassNotFoundException e) {
                            logger.log(Level.WARNING, String.format("Class %s not found in %s", classPath, jarFilePath));
                        }
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(String.format("Not found jar file %s", jarFilePath), ex);
            }
        }
        return classes;
    }
}
