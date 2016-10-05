package com.epam.dlab.auth.core;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExpirableContainer<M> {

    private static class Holder<M> {

        private final M data;
        private final long expTime;

        public Holder(M data, long ttl) {
            this.data = data;
            this.expTime = System.currentTimeMillis() + ttl;
        }

        public boolean expired() {
            return System.currentTimeMillis() > expTime;
        }

        public M get() {
            return data;
        }
    }

    private final Map<String, Holder<M>> map = new ConcurrentHashMap<>();

    public ExpirableContainer() {

    }

    public void put(String key, M data, long ttl) {
        if (ttl <= 0) {
            return;
        } else {
            map.put(key, new Holder<M>(data, ttl));
        }
    }

    public M get(String key) {
        Holder<M> dataHolder = map.get(key);
        if (dataHolder == null) {
            return null;
        } else if (dataHolder.expired()) {
            map.remove(key);
            return null;
        } else {
            return dataHolder.get();
        }
    }

    public void touchKeys() {
        ArrayList<String> allKeys = new ArrayList<>(map.keySet());
        allKeys.forEach(key -> get(key));
    }

}
