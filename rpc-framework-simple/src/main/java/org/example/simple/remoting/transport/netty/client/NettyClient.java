package org.example.simple.remoting.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.simple.remoting.dto.RpcRequest;
import org.example.simple.remoting.dto.RpcResponse;
import org.example.simple.serialize.kryo.KryoSerializer;
import org.example.simple.remoting.transport.netty.codec.kyro.NettyKryoDecoder;
import org.example.simple.remoting.transport.netty.codec.kyro.NettyKryoEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class NettyClient {

    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;

    // 初始化相关资源
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                // 连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 如果 5 秒之内没有发送数据给服务端，就发送一次心跳请求
                        ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        /*自定义序列化编解码器*/
                        // RpcResponse -> ByteBuf
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("客户端连接成功!");
                        completableFuture.complete(future.channel());
                    } else {
                        throw new IllegalStateException();
                    }
                });
        return completableFuture.get();
    }

    public static void close() {
        log.info("call close method");
        eventLoopGroup.shutdownGracefully();
    }
}
