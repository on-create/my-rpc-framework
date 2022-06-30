package org.example.simple.transport.netty.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.common.utils.concurrent.ThreadPoolFactoryUtil;
import org.example.simple.registry.DefaultServiceRegistry;
import org.example.simple.registry.ServiceRegistry;
import org.example.simple.transport.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * 自定义服务端的 ChannelHandler 来处理客户端发过来的数据
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static final RpcRequestHandler rpcRequestHandler;
    private static final ServiceRegistry serviceRegistry;
    private static final ExecutorService threadPool;

    static {
        rpcRequestHandler = new RpcRequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
        threadPool = ThreadPoolFactoryUtil.createDefaultThreadPool("netty-server-handler-rpc-pool");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*try {
            RpcRequest rpcRequest = (RpcRequest) msg;
            logger.info(String.format("server receive msg: %s", rpcRequest));
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = rpcRequestHandler.handle(rpcRequest, service);
            logger.info(String.format("server get result: %s", result.toString()));
            ChannelFuture f = null;
            if (result instanceof RpcResponse) {
                f = ctx.writeAndFlush(result);
            } else {
                f = ctx.writeAndFlush(RpcResponse.success(result));
            }
            f.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }*/

        threadPool.execute(() -> {
            logger.info(String.format("server handle message from client by thread: %s", Thread.currentThread().getName()));
            try {
                logger.info(String.format("server receive msg: %s", msg));
                RpcRequest rpcRequest = (RpcRequest) msg;
                String interfaceName = rpcRequest.getInterfaceName();
                // 通过注册中心获取到目标类（客户端需要调用类）
                Object service = serviceRegistry.getService(interfaceName);
                // 执行目标方法，并返回结果
                Object result = rpcRequestHandler.handle(rpcRequest, service);
                logger.info(String.format("server get result: %s", result.toString()));
                // 返回方法执行结果给客户端
                ChannelFuture f = null;
                if (result instanceof RpcResponse) {
                    f = ctx.writeAndFlush(result);
                } else {
                    f =ctx.writeAndFlush(RpcResponse.success(result));
                }
                f.addListener(ChannelFutureListener.CLOSE);
            } finally {
                // 确保 ByteBuf 被释放，不然可能会有内存泄露问题
                ReferenceCountUtil.release(msg);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
