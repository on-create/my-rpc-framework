package org.example.simple.remoting.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.common.enumeration.RpcMessageTypeEnum;
import org.example.common.factory.SingletonFactory;
import org.example.simple.remoting.dto.RpcRequest;
import org.example.simple.remoting.dto.RpcResponse;

import java.net.InetSocketAddress;

/**
 * 自定义客户端 ChannelHandler 来处理服务端发过来的数据
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequests unprocessedRequests;

    public NettyClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

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
            unprocessedRequests.complete(rpcResponse);
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
        log.error("client catch org.example.simple.remoting.transport.netty.client.NettyClientHandler.exception: ", cause);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress());
                RpcRequest rpcRequest = RpcRequest.builder()
                        .rpcMessageTypeEnum(RpcMessageTypeEnum.HEART_BEAT)
                        .build();
                channel.writeAndFlush(rpcRequest)
                        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
