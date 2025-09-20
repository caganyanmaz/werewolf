package com.caganyanmaz.werewolf.infrastructure;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class InMemoryRepository<T> {
    private final ConcurrentMap<String, T> store = new ConcurrentHashMap<>();
    private final Function<T, String> id_function;

    public InMemoryRepository(Function<T, String> id_function) {
        this.id_function = id_function;
    }

    public T get(String id) {
        T v = store.get(id);
        if (v == null) {
            throw new IllegalArgumentException("Entity with id " + id + " not found");
        }
        return v;
    }

    public void save(T entity) {
        String id = id_function.apply(entity);
        T prev = store.putIfAbsent(id, entity);
        if (prev != null) {
            throw new IllegalArgumentException("Entity with id " + id + " already exists");
        }
    }

    public void update(T entity) {
        String id = id_function.apply(entity);
        T prev = store.replace(id, entity);
        if (prev == null) {
            throw new IllegalArgumentException("Entity with id " + id + " not found for update");
        }
    }

    public boolean exists(String id) { return store.containsKey(id); }

    public void delete(String id) { store.remove(id); }
}
