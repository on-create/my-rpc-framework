package org.example.simple.transport.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.RpcResponse;

/**
 * 自定义客户端 ChannelHandler 来处理服务端发过来的数据
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取服务端传输的消息
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info(String.format("client receive msg: %s", msg));
            RpcResponse<?> rpcResponse = (RpcResponse<?>) msg;
            // 声明一个 AttributeKey 对象
            AttributeKey<RpcResponse<?>> key = AttributeKey.valueOf("rpcResponse" + rpcResponse.getRequestId());
            /**
             * AttributeMap 可以看作是一个 Channel 的共享数据源
             * AttributeMap的key是AttributeKey，value是Attribute
             * 将服务端的返回结果保存到 AttributeMap 上
             */
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理客户端消息发生异常的时候被调用
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch org.example.simple.transport.netty.client.NettyClientHandler.exception: ", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
