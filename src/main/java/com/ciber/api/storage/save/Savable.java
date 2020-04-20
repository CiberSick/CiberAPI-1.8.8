package com.ciber.api.storage.save;

public interface Savable<T> {
    Object save(T object);
    T pull(Object object, Class<T> tClass);
}
