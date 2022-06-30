package org.example.simple.serialize;

/**
 * 序列化接口，所有序列化类都要实现这个接口
 */
public interface Serializer {

    /**
     * 序列化
     * @param obj 要序列化的对象
     * @return  字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     * @param bytes 序列化后的字节数组
     * @param clazz 类
     * @param <T>
     * @return 反序列的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
