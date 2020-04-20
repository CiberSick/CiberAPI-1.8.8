//package com.ciber.api.manager;
//
//import com.ciber.api.object.CiberConfig;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//import java.util.*;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public abstract class Manager<T> implements Iterator<T> {
//
//    private ManagerModel configuration;
//    protected transient CiberConfig storage;
//
//    protected List<T> objects = new LinkedList<T>();
//
//    protected Optional<T> findObjectOptional(List<T> objects, Predicate<T> condition) {
//        return findObjectStream(objects, condition).findFirst();
//    }
//
//    protected T findObject(Predicate<T> condition) {
//        return findObjectOptional(objects, condition).get();
//    }
//
//    protected Optional<T> findObjectOptional(Predicate<T> condition) {
//        return findObjectOptional(objects, condition);
//    }
//
//    protected List<T> findObjectList(Predicate<T> condition) {
//        return findObjectStream(objects, condition).collect(Collectors.toList());
//    }
//
//    protected List<T> findObjectList(List<T> objects, Predicate<T> condition) {
//        return findObjectStream(objects, condition).collect(Collectors.toList());
//    }
//
//    protected Stream<T> findObjectStream(Predicate<T> condition) {
//        return findObjectStream(objects, condition);
//    }
//
//    protected Stream<T> findObjectStream(List<T> objects, Predicate<T> condition) {
//        return objects.stream().filter(condition);
//    }
//
//    public abstract void loadDefaults();
//
//    public Manager() {
//
//    }
//
//    public CiberConfig getStorage() {
//        return storage;
//    }
//
//    public void setStorage(CiberConfig storage) {
//        this.storage = storage;
//    }
//
//    public void setConfiguration(ManagerModel configuration) {
//        this.configuration = configuration;
//    }
//
//    @SuppressWarnings("unchecked")
//    public <E extends ManagerModel> E getConfiguration(Class<E> cls) {
//        return (E) configuration;
//    }
//
//    @SuppressWarnings("unchecked")
//    public <E extends ManagerModel> E getConfiguration() {
//        return (E) getConfiguration(ManagerModel.class);
//    }
//
//    public static class ManagerSerializable<T> implements Savable<Manager<T>> {
//        @SuppressWarnings({"unchecked"})
//        @Override
//        public Manager<T> deserialize(Map<String, Object> data) {
//            Manager<T> result = null;
//            try {
//                Class<Manager<T>> aClass = (Class<Manager<T>>) Class.forName((String) data.get(SavableAPI.TYPE_KEY));
//                Constructor<Manager<T>> constructor = aClass.getConstructor();
//                constructor.setAccessible(true);
//                result = constructor.newInstance();
//                result.objects = ((List<Map<String, Object>>) data.get("objects")).stream().map(objectMap -> (T) SavableAPI.deserialize(objectMap, null)).collect(Collectors.toList());
//                result.configuration = (ManagerModel) SavableAPI.deserialize((Map<String, Object>) data.get("configuration"), null);
//                assert result.configuration != null;
//                result.configuration.loadDefaults();
//            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
//            return result;
//        }
//
//        @Override
//        public Map<String, Object> serialize(Manager<T> data) {
//            Map<String, Object> result = new LinkedHashMap<>();
//            result.put(SavableAPI.TYPE_KEY, data.getClass().getName());
//            result.put("configuration", SavableAPI.serialize(data.configuration));
//            result.put("objects", data.objects.stream().map(SavableAPI::serialize).collect(Collectors.toList()));
//            return result;
//        }
//    }
//
//    public Manager(List<T> objects) {
//        this();
//        this.setObjects(objects);
//    }
//
//    public List<T> getObjects() {
//        return objects;
//    }
//
//    public void setObjects(List<T> objects) {
//        this.objects = objects;
//    }
//
//    public Iterator<T> iterator() {
//        return objects.iterator();
//    }
//}
