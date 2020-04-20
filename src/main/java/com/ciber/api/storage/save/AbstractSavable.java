package com.ciber.api.storage.save;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("rawtype")
public abstract class AbstractSavable<T> implements Savable<T> {

    public Object save(T object) {
        return SavableAPI.getInstance().save(object, false);
    }

    @SuppressWarnings("unchecked")

    public Object save() {
        return save((T) this);
    }

    @SuppressWarnings("unchecked")
    public T createInstance(Class<? extends T> aClass) {
        try {
            Constructor<?> constructor = aClass.getConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (NoSuchMethodException e) {
            System.err.printf("No constructor found in %s.", aClass.getName());
        } catch (IllegalAccessException e) {
            System.err.printf("The constructor is private or protected in %s.", aClass.getName());
        } catch (InstantiationException e) {
            System.err.printf("An exception was occurred when instantiate %s.", aClass.getName());
        } catch (InvocationTargetException e) {
            System.err.printf("An exception was occurred when invoke target %s.", aClass.getName());
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s@%d", getClass().getSimpleName(), hashCode());
    }

    @SuppressWarnings("unchecked")

    public T pull(Object object, Class tClass) {
        return (T) SavableAPI.getInstance().pull(object, tClass, false);
    }

    public T pull(Object object) {
        return pull(object, getClass());
    }

}
