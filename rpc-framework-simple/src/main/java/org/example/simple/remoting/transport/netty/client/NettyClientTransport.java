package org.example.simple.remoting.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.example.common.factory.SingletonFactory;
import org.example.simple.remoting.dto.RpcRequest;
import org.example.simple.remoting.dto.RpcResponse;
import org.example.simple.remoting.dto.RpcMessageChecker;
import org.example.simple.registry.ServiceDiscovery;
import org.example.simple.registry.ZkServiceDiscovery;
import org.example.simple.remoting.transport.ClientTransport;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class NettyClientTransport implements ClientTransport {

    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;

    public NettyClientTransport() {
        this.serviceDiscovery = new ZkServiceDiscovery();
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /*@Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        AtomicReference<Object> result = new AtomicReference<>(null);
        try {
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress);
            // isActive(): 通过查找底层套接字来查看它是否已经连接来工作
            if (!channel.isActive()) {
                NettyClient.close();
                return null;
            }

            channel.writeAndFlush(rpcRequest)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            log.info("client send message: {}", rpcRequest);
                        } else {
                            future.channel().close();
                            log.error("Send failed:", future.cause());
                        }
                    });

            channel.closeFuture().sync();
            AttributeKey<RpcResponse<?>> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
            RpcResponse<?> rpcResponse = channel.attr(key).get();
            log.info("client get rpcResponse from channel:{}", rpcResponse);
            // 校验 RpcResponse 和 RpcRequest
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            result.set(rpcResponse.getData());
        } catch (InterruptedException e) {
            log.error("occur exception when send rpc message from client:", e);
        }

        return result.get();
    }*/

    @Override
    public CompletableFuture<RpcResponse<?>> sendRpcRequest(RpcRequest rpcRequest) {
        // 构建返回值
        CompletableFuture<RpcResponse<?>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        Channel channel = ChannelProvider.get(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            // 放入未处理的请求
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            log.info("client send message: [{}]", rpcRequest);
                        } else {
                            future.channel().close();
                            resultFuture.completeExceptionally(future.cause());
                            log.error("Send failed:", future.cause());
                        }
                    });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }
}
