package org.example.common.utils.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取单例对象的工厂类
 */
public final class SingletonFactory {

    private static final Map<String, Object> objectMap = new HashMap<>();
    private static volatile Object instance;

    private SingletonFactory() {

    }

    public static <T> T getInstance(Class<T> c) {
        String key = c.toString();
        instance = objectMap.get(key);
        if (instance == null) {
            synchronized (c) {
                if (instance == null) {
                    try {
                        instance = c.newInstance();
                        objectMap.put(key, instance);
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
        }
        return c.cast(instance);
    }
}
