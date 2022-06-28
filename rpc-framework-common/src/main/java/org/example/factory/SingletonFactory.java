package org.example.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取单例对象的工厂类
 */
public final class SingletonFactory {

    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory() {

    }

    public static <T> T getInstance(Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }

        // c --> ArrayList.class key: class java.util.ArrayList
        String key = c.toString();
        if (OBJECT_MAP.containsKey(key)) {
            // cast(): 强制类型转换
            return c.cast(OBJECT_MAP.get(key));
        } else {
            return c.cast(OBJECT_MAP.computeIfAbsent(key, k -> {
                try {
                    /**
                     * 获取 c 类的无参构造函数，创建单例对象
                     * 例如：ArrayList public java.util.ArrayList()
                     */
                    return c.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }));
        }
    }
}
