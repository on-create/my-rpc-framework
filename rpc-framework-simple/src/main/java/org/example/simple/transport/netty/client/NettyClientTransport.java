package org.example.simple.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.common.utils.checker.RpcMessageChecker;
import org.example.simple.transport.ClientTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public class NettyClientTransport implements ClientTransport {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientTransport.class);
    private final InetSocketAddress inetSocketAddress;

    public NettyClientTransport(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        AtomicReference<Object> result = new AtomicReference<>(null);
        try {
            Channel channel = ChannelProvider.get(inetSocketAddress);
            // isActive(): 通过查找底层套接字来查看它是否已经连接来工作
            if (channel.isActive()) {
                channel.writeAndFlush(rpcRequest)
                        .addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                logger.info("client send message: {}", rpcRequest);
                            } else {
                                future.channel().close();
                                logger.error("Send failed:", future.cause());
                            }
                        });

                channel.closeFuture().sync();
                AttributeKey<RpcResponse<?>> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse<?> rpcResponse = channel.attr(key).get();
                logger.info("client get rpcResponse from channel:{}", rpcResponse);
                // 校验 RpcResponse 和 RpcRequest
                RpcMessageChecker.check(rpcRequest, rpcResponse);
                result.set(rpcResponse.getData());
            } else {
                System.exit(0);
            }
        } catch (InterruptedException e) {
            logger.error("occur exception when send rpc message from client:", e);
        }

        return result.get();
    }
}
