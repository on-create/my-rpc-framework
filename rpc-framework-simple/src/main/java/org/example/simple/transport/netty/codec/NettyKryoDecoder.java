package org.example.simple.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.simple.serialize.Serializer;

import java.util.List;

/**
 * 自定义解码器
 * 处理“入站”消息，将消息格式转换为我们需要的业务对象
 */
@Slf4j
@AllArgsConstructor
public class NettyKryoDecoder extends ByteToMessageDecoder {

    private final Serializer serializer;
    private final Class<?> genericClass;

    /**
     * Netty传输的消息长度也就是对象序列化后对应的字节数组的大小，存储在 ByteBuf 头部
     */
    private static final int BODY_LENGTH = 4;

    /**
     * 解码
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 1.byteBuf中写入的消息长度所占的字节数已经是4了，所以 byteBuf 的可读字节必须大于4
        if (byteBuf.readableBytes() >= BODY_LENGTH) {
            // 2.标记当前 readIndex 的位置，以便后面重置 readIndex 的时候使用
            byteBuf.markReaderIndex();
            // 3.读取消息的长度
            // 注意：消息长度是 encode 的时候我们自己写入的，参见 NettyKryoEncoder 的encode方法
            int dataLength = byteBuf.readInt();
            // 4.遇到不合理的情况直接 return
            if (dataLength < 0 || byteBuf.readableBytes() < 0) {
                log.error("data length or byteBuf readableBytes is not valid");
                return;
            }
            // 5.如果可读字节数小于消息长度的话，说明是不完整的消息，重置readIndex
            if (byteBuf.readableBytes() < dataLength) {
                byteBuf.resetReaderIndex();
                return;
            }
            // 6.走到这里说明没什么问题了，可以序列化了
            byte[] body = new byte[dataLength];
            byteBuf.readBytes(body);
            // 将bytes数组转换为我们需要的对象
            Object obj = serializer.deserialize(body, genericClass);
            list.add(obj);
            log.info("successful decode ByteBuf to Object");
        }
    }
}
