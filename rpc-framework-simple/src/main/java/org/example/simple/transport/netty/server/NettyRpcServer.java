package org.example.simple.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.simple.serialize.kryo.KryoSerializer;
import org.example.simple.transport.netty.codec.NettyKryoDecoder;
import org.example.simple.transport.netty.codec.NettyKryoEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端
 * 接收客户端消息，根据客户端的消息调用相应的方法，将结果返回给客户端
 */
public class NettyRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);
    private final int port;
    private final KryoSerializer kryoSerializer;

    public NettyRpcServer(int port) {
        this.port = port;
        this.kryoSerializer = new KryoSerializer();
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                            ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    })
                    // 设置tcp缓冲区
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，同步等待绑定成功
            ChannelFuture f = b.bind(port).sync();
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("occur org.example.simple.transport.netty.server.NettyRpcServer.exception when start server:", e);
        }
    }
}
