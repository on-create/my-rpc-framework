package org.example.simple.remoting.transport.netty.codec.kyro;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import org.example.simple.serialize.Serializer;

/**
 * 自定义解码器
 * 处理“出站”消息，将消息格式转换为字节数组然后写入到字节数据的容器 ByteBuf 对象中
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {

    private final Serializer serializer;
    private final Class<?> genericClass;

    /**
     * 将对象转换为字节码然后写入到 ByteBuf 对象中
     * @param channelHandlerContext
     * @param o
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        // 等效于 o instanceof genericClass
        if (genericClass.isInstance(o)) {
            // 1.将对象转换为byte
            byte[] body = serializer.serialize(o);
            // 2.读取消息的长度
            int dataLength = body.length;
            // 3.写入消息对应的字节数组长度，writerIndex 加4
            byteBuf.writeInt(dataLength);
            // 4.将字节数组写入 ByteBuf 对象中
            byteBuf.writeBytes(body);
        }
    }
}
