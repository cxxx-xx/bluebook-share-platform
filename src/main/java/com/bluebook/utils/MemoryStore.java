package com.bluebook.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class MemoryStore {
    
    private final Map<String, String> store = new ConcurrentHashMap<>();
    private final Map<String, Long> expiry = new ConcurrentHashMap<>();
    
    public void set(String key, String value, long timeout, TimeUnit unit) {
        store.put(key, value);
        expiry.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
    }
    
    public String get(String key) {
        Long expireTime = expiry.get(key);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            store.remove(key);
            expiry.remove(key);
            return null;
        }
        return store.get(key);
    }
    
    public void delete(String key) {
        store.remove(key);
        expiry.remove(key);
    }
    
    public boolean exists(String key) {
        Long expireTime = expiry.get(key);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            store.remove(key);
            expiry.remove(key);
            return false;
        }
        return store.containsKey(key);
    }
}
