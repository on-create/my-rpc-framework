package org.example.simple.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.common.exception.SerializeException;
import org.example.simple.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Kryo序列化类，Kryo序列化效率高，但只兼容 java
 */
@Slf4j
public class KryoSerializer implements Serializer {

    /**
     * ThreadLocal.withInitial: 当threadLocal中不存在值时，调用get()，会返回使用初始方法生成的对象
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        // 默认值为true，是否关闭注册行为，关闭之后可能存在序列化问题（序列号问题），一般推荐设置为 true
        kryo.setRegistrationRequired(false);
        // 默认值为false，是否关闭循环引用，可以提高性能，但是一般不推荐设置为 true
        kryo.setReferences(true);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // Object -> byte: 将对象序列化为byte数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (IOException e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte -> Object: 从byte数组中反序列化出对象
            Object obj = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(obj);
        } catch (IOException e) {
            throw new SerializeException("反序列化失败");
        }
    }
}
