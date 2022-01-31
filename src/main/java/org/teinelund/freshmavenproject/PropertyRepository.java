package org.teinelund.freshmavenproject;

import java.util.HashMap;
import java.util.Map;

public class PropertyRepository {

    private Map<String, Object> properties = new HashMap<>();

    public void put(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object get(String key) {
        return this.properties.get(key);
    }

    public boolean containsNotProperty(String key) {
        return !properties.containsKey(key);
    }
}
