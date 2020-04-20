package com.ciber.api.storage.save;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SimpleManager<T> extends AbstractSavable<SimpleManager<T>> implements Iterable<T> {

    private List<T> objects;

    public abstract void loadDefaults();

    public SimpleManager() {
        loadDefaults();
    }

    protected Stream<T> findStream(Predicate<T> condition) {
        return objects.stream().filter(condition);
    }

    protected List<T> findList(Predicate<T> condition) {
        return findStream(condition).collect(Collectors.toList());
    }

    protected T findObject(Predicate<T> condition) {
        return findOptional(condition).orElse(null);
    }

    protected Optional<T> findOptional(Predicate<T> condition) {
        return findStream(condition).findFirst();
    }

    @Override
    public Iterator<T> iterator() {
        return objects.iterator();
    }

    public List<T> getObjects() {
        return objects;
    }

    public void setObjects(List<T> objects) {
        this.objects = objects;
    }
}
