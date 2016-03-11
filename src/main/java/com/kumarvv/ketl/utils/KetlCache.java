package com.kumarvv.ketl.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KetlCache {

    private static final int MAX_SIZE = 25000;
    private static KetlCache _instance = null;

    private final ConcurrentMap<String, Object> _cache;

    public KetlCache() {
        this._cache = new ConcurrentHashMap<>();
    }

    public static KetlCache getInstance() {
        if (_instance == null) {
            _instance = new KetlCache();
        }
        return _instance;
    }

    public boolean exists(String key) {
        if (key == null) {
            return false;
        }
        return _cache.containsKey(key);
    }

    public Object get(String key) {
        if (key == null) {
            return null;
        }
        return _cache.get(key);
    }

    public void set(String key, Object val) {
        if (_cache == null || key == null || val == null) {
            return;
        }
        synchronized (_cache) {
            if (_cache.size() < MAX_SIZE) {
                _cache.put(key, val);
            }
        }
    }

    public int size() {
        return _cache.size();
    }

    public void clear() {
        synchronized (_cache) {
            _cache.clear();
        }
    }

}
