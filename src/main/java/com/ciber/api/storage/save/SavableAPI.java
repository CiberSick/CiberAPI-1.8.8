package com.ciber.api.storage.save;

import com.ciber.api.CiberAPI;
import com.google.common.annotations.Beta;
import com.google.common.collect.Maps;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ciber.api.storage.save.ReflectionUtils.set;

public class SavableAPI {


    private static SavableAPIHolder instance;

    @SuppressWarnings({"rawtypes", "unused"})
    public interface ISavableAPI {

        Logger getLogger();

        SaveOptions getOptions();

        Map<Class<?>, Savable> getReplaces();

        List<Class<?>> getSavables();

        Map<String, Object> getObjects();

        Map<String, Class<?>> getAliases();

        void setOptions(SaveOptions options);

        void setReplaces(Map<Class<?>, Savable> options);

        void setSavables(List<Class<?>> options);

        void setObjects(Map<String, Object> options);

        void setAliases(Map<String, Class<?>> options);

        boolean hasSavable(Class<?> aClass);

        Object getObjectById(String id);

        String getIdByObject(Object savable);

        String getOrDefaultIdByObject(Object savable, String def);

        <T> T pull(Object data, Class<T> tClass, boolean inline);

        Object save(Object data, boolean inline);

        Class<?> getClassByAlias(String classAlias);

        String createAndAddAlias(Class<?> aClass);

        void registerClass(Class<?> aClass);

        void registerClass(Class<?> aClass, Savable savable);

        void registerPackage(String aPackage, Class<?> thisClass);

        void registerPackage(String aPackage, Class<?> thisClass, Savable savable);

        void registerClasses(List<Class<?>> classes, Class<?> thisClass);

        void registerClasses(List<Class<?>> classes, Class<?> thisClass, Savable savable);

    }

    @SuppressWarnings("rawtypes")
    private static class SavableAPIHolder implements ISavableAPI {


        private SaveOptions options;

        private Map<Class<?>, Savable> replaces;

        private List<Class<?>> savables;

        private Map<String, Object> objects;

        private Map<String, Class<?>> aliases;

        private final Logger logger;

        private SavableAPIHolder() {
            logger = Logger.getLogger("Minecraft");
            replaces = Maps.newHashMap();
            savables = new ArrayList<>();
            objects = Maps.newHashMap();
            aliases = Maps.newHashMap();
            registerClass(List.class, new ListSavable(this));
            registerClass(Map.class, new MapSavable(this));
            options = new SaveOptions();
        }

        @Override
        public void registerClass(Class<?> aClass) {
            registerClass(aClass, new ObjectSavable(this));
        }

        @Override
        public void registerClass(Class<?> aClass, Savable savable) {
            replaces.put(aClass, savable);
            savables.add(aClass);
        }

        @Override
        public void registerPackage(String aPackage, Class<?> thisClass) {
            registerPackage(aPackage, thisClass, replaces.get(Object.class));
        }

        @Override
        public void registerPackage(String aPackage, Class<?> thisClass, Savable savable) {
            registerClasses(ReflectionUtils.getClasses(aPackage, thisClass), thisClass, savable);
        }

        @Override
        public void registerClasses(List<Class<?>> classes, Class<?> thisClass) {
            registerClasses(classes, thisClass, replaces.get(Object.class));
        }

        @Override
        public void registerClasses(List<Class<?>> classes, Class<?> thisClass, Savable savable) {
            classes.forEach(cls -> registerClass(thisClass, savable));
        }

        @Override
        public boolean hasSavable(Class<?> aClass) {
            try {
                getSavable(aClass);
                return true;
            } catch (NullPointerException ex) {
                return false;
            }
        }

        @Override
        public Object getObjectById(String id) {
            return objects.entrySet().stream().filter(it -> it.getKey().contentEquals(id)).collect(Collectors.toList()).get(0).getValue();
        }

        @Override
        public String getIdByObject(Object object) {
            return objects.entrySet().stream().filter(it -> it.getValue().equals(object)).collect(Collectors.toList()).get(0).getKey();
        }

        @Override
        public String getOrDefaultIdByObject(Object savable, String def) {
            return getIdByObject(savable) != null ? getIdByObject(savable) : "";
        }

        private Savable getSavable(Class aClass) {
            Optional<Map.Entry<Class<?>, Savable>> opt = replaces.entrySet()
                    .stream()
                    .filter(entry -> ReflectionUtils.getSuperClasses(aClass).contains(entry.getKey()) || aClass.getName().contentEquals(entry.getKey().getName()))
                    .findFirst();
            if (opt.isPresent()) {
                try {
                    List<Class<?>> classes = ReflectionUtils.getSuperClasses(aClass);
                    classes.add(aClass);
                    return replaces.get(classes.stream().filter(it -> replaces.containsKey(it)).collect(Collectors.toList()).get(0));
                } catch (Exception ex) {
                    logger.severe("Error when getting class savable " + aClass.getName());
                }
            }
            return new Savable() {
                @Override
                public Object save(Object data) {
                    if (data instanceof ConfigurationSerializable) {
                        return ((ConfigurationSerializable) data).serialize();
                    }
                    if (!(data instanceof Serializable)) {
                        return createId(data);
                    }
                    return data;
                }

                @SuppressWarnings("unchecked")
                @Override
                public Object pull(Object data, Class aClass) {
                    if (data == null) {
                        return null;
                    }
                    if (aClass != null) {
                        if (aClass.isAssignableFrom(ConfigurationSerializable.class)) {
                            return ConfigurationSerialization.deserializeObject((Map<String, ?>) data);
                        }
                        if (aClass.isAssignableFrom(Serializable.class)) {
                            return getObjectById(((String) data));
                        }
                    }
                    return data;
                }
            };

        }

        @Override
        @SuppressWarnings("unchecked")

        public Object save(Object data, boolean inline) {
            Object result = data;

            try {
                if (hasSavable(data.getClass())) {
                    if (inline) {
                        result = new InlineSavable(this).save(data);
                    } else {
                        result = getSavable(data.getClass()).save(data);
                    }
                }
            } catch (NullPointerException ex) {
                logger.severe("Cannot save " + data);
            }
            return result;
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public <T> T pull(Object data, Class<T> tClass, boolean inline) {
            T result = null;
            if (tClass != null) {
                if (hasSavable(tClass)) {
                    if (inline) {
                        result = (T) new InlineSavable(this).pull(data, tClass);
                    } else {
                        result = (T) getSavable(tClass).pull(data, tClass);
                    }
                }
            }
            return result;
        }

        @Override
        public Class<?> getClassByAlias(String classAlias) {
            return aliases.get(classAlias);
        }

        @Override
        public String createAndAddAlias(Class<?> aClass) {
            String alias = createAlias(aClass, aliases);
            if (!aliases.containsKey(alias))
                aliases.put(alias, aClass);
            return alias;
        }

        private String getIdSecondPart(int hashCode) {
            return objects.entrySet().stream().filter(
                    it -> it.getKey().split(options.referKey)[1].contentEquals(String.valueOf(hashCode))
            ).collect(Collectors.toList()).get(0).getKey().split(options.referKey)[1];
        }


        private String createId(Object object) {
            String id1 = createAlias(object.getClass(), objects);
            int id2 = object.hashCode();
            Random generator = new Random();
            String id2Final = Integer.toHexString(id2);
            if (objects.containsKey(String.format("%s%s%s", id1, options.referKey, id2Final))) {
                while (!getIdSecondPart(object.hashCode()).equals(String.valueOf(id2))) {
                    id2 += object.hashCode() * generator.nextInt(25304293);
                }
            }
            id2Final = Integer.toHexString(id2);
            String id = String.format("%s%s%s", id1, options.referKey, id2Final);
            objects.put(id, object);
            return id;
        }

        public String createAlias(Class<?> aClass, Map<?, ?> map) {
            String rawId;
            if (aClass.isAnnotationPresent(SaveAlias.class)) {
                SaveAlias id = aClass.getAnnotation(SaveAlias.class);
                if (map.containsKey(id.value())) {
                    return id.value();
                } else {
                    rawId = id.value();
                }
            } else {
                rawId = aClass.getName();
            }
            return rawId;
        }

        @Override

        public Map<String, Object> getObjects() {
            return objects;
        }

        @Override
        public Map<String, Class<?>> getAliases() {
            return aliases;
        }

        @Override
        public void setObjects(Map<String, Object> objects) {
            this.objects = objects;
        }

        @Override
        public void setAliases(Map<String, Class<?>> aliases) {
            this.aliases = aliases;
        }


        @Override
        public Logger getLogger() {
            return logger;
        }


        public SaveOptions getOptions() {
            return options;
        }

        @Override
        public void setOptions(SaveOptions options) {
            this.options = options;
        }

        @Override

        public Map<Class<?>, Savable> getReplaces() {
            return replaces;
        }

        @Override
        public void setReplaces(Map<Class<?>, Savable> replaces) {
            this.replaces = replaces;
        }

        @Override

        public List<Class<?>> getSavables() {
            return savables;
        }

        @Override
        public void setSavables(List<Class<?>> savables) {
            this.savables = savables;
        }

        private static class ObjectSavable implements Savable<Object> {

            private ISavableAPI api;

            public ObjectSavable(ISavableAPI api) {
                this.api = api;
            }

            @Override
            public Object save(Object object) {
                if (object == null) {
                    return null;
                }
                Map<String, Object> result = Maps.newLinkedHashMap();
                Class<?> aClass = object.getClass();
                result.put(api.getOptions().typeKey, api.createAndAddAlias(aClass));
                AtomicReference<Object> value = new AtomicReference<>(null);
                AtomicReference<String> name = new AtomicReference<>("");
                List<Field> properties = ReflectionUtils.getProperties(aClass, it -> true);

                properties.stream()
                        .filter(prop -> !(Modifier.isTransient(prop.getModifiers()) || Modifier.isStatic(prop.getModifiers()) || Modifier.isFinal(prop.getModifiers())))
                        .forEach(prop -> {

                    if (prop.isAnnotationPresent(SaveIdentification.class)) {
                        SaveIdentification annotation = prop.getAnnotation(SaveIdentification.class);
                        name.set(annotation.value());
                    } else name.set(prop.getName());
                    prop.setAccessible(true);
                    if (api.hasSavable(prop.getType())) {
                        value.set(api.save(ReflectionUtils.get(aClass, prop, object), false));
                    } else if (value.get().getClass().isArray()) {
                        value.set(Arrays.asList((Object[]) value.get()));
                    } else if (value.get().getClass().isEnum()) {
                        value.set(((Enum) value.get()).name());
                    }
                    result.put(name.get(), value.get());
                });
                return result;
            }

            @SuppressWarnings({"unchecked"})

            @Override
            public Object pull(Object object, Class<Object> aClass) {
                if (object == null) {
                    return null;
                }
                Map<String, Object> map = (Map<String, Object>) object;

                if (aClass == null) {
                    aClass = (Class<Object>) api.getClassByAlias((String) map.get(api.getOptions().typeKey));
                }

                map.remove(api.getOptions().typeKey);

                try {
                    Constructor<?> constructor = aClass.getConstructor();
                    if (constructor.isAccessible()) {
                        api.getLogger().log(Level.WARNING, String.format("Constructor in %s is private or protected.", aClass.getName()));
                    }
                    constructor.setAccessible(true);
                    Object instance = constructor.newInstance();
                    List<Field> properties = ReflectionUtils.getProperties(aClass, it -> api.hasSavable(it));
                    Object value;
                    Object orig = null;
                    String name;
                    Class<?> aType;
                    for (Field prop : properties) {
                        if ((prop.isAnnotationPresent(SaveIdentification.class) && map.containsKey(prop.getAnnotation(SaveIdentification.class).value()))) {
                            name = prop.getAnnotation(SaveIdentification.class).value();
                        } else {
                            name = prop.getName();
                        }
                        if (map.containsKey(name)) {
                            String finalName = name;
                            aType = prop.getType();
                            value = map.entrySet().stream().filter(it -> it.getKey().contentEquals(finalName)).collect(Collectors.toList()).get(0).getValue();
                            if (value instanceof Map) {
                                aType = api.getClassByAlias((String) ((Map) value).get(api.getOptions().typeKey));
                            }
                            value = api.pull(value, aType, false);
                            try {
                                orig = prop.get(instance);
                                if (orig != null) {
                                    if (orig.getClass().isArray()) {
                                        value = (((List<Object>) value).toArray());
                                    } else if (orig.getClass().isEnum()) {
                                        value = orig.getClass().getDeclaredField(((String) value).toUpperCase()).get(null);
                                    }
                                }
                            } catch (IllegalAccessException ex) {
                                api.getLogger().log(Level.WARNING, String.format("The field is private in %s when deserializing %s.", orig.getClass().getName(), object.toString()), ex);
                            } catch (NoSuchFieldException ex) {
                                api.getLogger().log(Level.WARNING, String.format("No enum field found in %s when deserializing %s.", orig.getClass().getName(), object.toString()), ex);
                            }
                            set(aClass, prop, instance, value);
                        }
                    }
                    return instance;
                } catch (NoSuchMethodException ex) {
                    api.getLogger().log(Level.WARNING, String.format("No constructor found in %s when deserializing %s.", aClass.getName(), object.toString()), ex);
                } catch (InstantiationException ex) {
                    api.getLogger().log(Level.WARNING, String.format("An error occurred when instantiate %s in %s when deserializing.", object.toString(), aClass.getName()), ex);
                } catch (IllegalAccessException ex) {
                    api.getLogger().log(Level.WARNING, String.format("The constructor or class is private or protected in %s when deserializing %s.", aClass.getName(), object.toString()), ex);
                } catch (InvocationTargetException ex) {
                    api.getLogger().log(Level.WARNING, String.format("An error occurred when execute constructor in %s when deserializing %s.", aClass.getName(), object.toString()), ex);
                }
                return null;
            }

        }

        private static class ListSavable implements Savable<List<Object>> {

            private ISavableAPI api;

            public ListSavable(ISavableAPI api) {
                this.api = api;
            }


            @Override
            public Object save(List<Object> object) {
                if (object == null) {
                    return null;
                }
                return object.stream().map(o -> api.save(o, false)).collect(Collectors.toList());
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Object> pull(Object object, Class<List<Object>> listClass) {
                if (object == null) {
                    return null;
                }
                return ((List<Object>) object).stream().map(o -> {
                    Class<?> aClass = o.getClass();
                    if (o instanceof Map) {
                        aClass = api.getClassByAlias((String) ((Map) o).get(api.getOptions().typeKey));
                    } else if (o.getClass().isAssignableFrom(String.class)) {
                        try {
                            return api.getObjectById((String) o);
                        } catch (Exception ignored) {
                            System.err.println("Cannot get by id " + o);
                        }
                    }
                    return api.pull(o, aClass, false);
                }).collect(Collectors.toList());
            }

        }

        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        @Beta
        private static class InlineSavable<T> implements Savable<T> {

            private ISavableAPI api;

            public InlineSavable(ISavableAPI api) {
                this.api = api;
            }


            @Override
            public Object save(T object) {
                return null;
            }


            @Override
            public T pull(Object object, Class<T> objectClass) {
                return null;
            }

        }

        private static class MapSavable implements Savable<Map<String, Object>> {

            private ISavableAPI api;

            public MapSavable(ISavableAPI api) {
                this.api = api;
            }


            @Override
            public Object save(Map<String, Object> object) {
                if (object == null) {
                    return null;
                }
                Map<String, Object> map = Maps.newLinkedHashMap();
                for (Map.Entry<String, Object> entry : object.entrySet()) {
                    map.put(entry.getKey(), api.save(entry.getValue(), false));
                }
                return map;
            }

            @SuppressWarnings("unchecked")

            @Override
            public Map<String, Object> pull(Object object, Class<Map<String, Object>> mapClass) {
                if (object == null) {
                    return null;
                }
                return ((Map<String, Object>) object).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> api.pull(entry.getValue(), null, false), (a, b) -> b, Maps::newLinkedHashMap));
            }

        }

        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        private static class EnumSavable implements Savable<Object> {

            private ISavableAPI api;

            public EnumSavable(ISavableAPI api) {
                this.api = api;
            }

            @Override
            public Object save(Object object) {
                return null;
            }


            @Override
            public Object pull(Object object, Class<Object> objectClass) {
                return null;
            }

        }
    }

    @SuppressWarnings("unused")
    private static class SaveOptions {
        private String typeKey = ConfigurationSerialization.SERIALIZED_TYPE_KEY;
        private String referKey = "@";

        public String getReferKey() {
            return referKey;
        }

        public String getTypeKey() {
            return typeKey;
        }

        public void setReferKey(String referKey) {
            this.referKey = referKey;
        }

        public void setTypeKey(String typeKey) {
            this.typeKey = typeKey;
        }
    }

    public static ISavableAPI getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new SavableAPIHolder();
        return instance;
    }
}
