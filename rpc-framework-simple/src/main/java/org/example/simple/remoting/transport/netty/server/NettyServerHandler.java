package org.example.simple.remoting.transport.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.common.factory.SingletonFactory;
import org.example.simple.remoting.dto.RpcRequest;
import org.example.simple.remoting.dto.RpcResponse;
import org.example.common.utils.concurrent.ThreadPoolFactoryUtil;
import org.example.simple.handler.RpcRequestHandler;

import java.util.concurrent.ExecutorService;

/**
 * 自定义服务端的 ChannelHandler 来处理客户端发过来的数据
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final String THREAD_NAME_PREFIX = "netty-server-handler-rpc-pool";
    private final RpcRequestHandler rpcRequestHandler;
    private final ExecutorService threadPool;

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        this.threadPool = ThreadPoolFactoryUtil.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        threadPool.execute(() -> {
            try {
                log.info("server receive msg: [{}] ", msg);
                RpcRequest rpcRequest = (RpcRequest) msg;
                // 执行目标方法，并返回结果
                Object result = rpcRequestHandler.handle(rpcRequest);
                log.info(String.format("server get result: %s", result.toString()));
                if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                    // 返回方法执行结果给客户端
                    if (result instanceof RpcResponse) {
                        ctx.writeAndFlush(result);
                    } else {
                        ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId()));
                    }
                } else {
                    log.error("not writable now, message dropped");
                }
            } finally {
                // 确保 ByteBuf 被释放，不然可能会有内存泄露问题
                ReferenceCountUtil.release(msg);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server org.example.simple.remoting.transport.netty.server.NettyServerHandler.catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
