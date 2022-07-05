package org.example.simple.remoting.transport.netty.client;

import org.example.simple.remoting.dto.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未处理的请求
 */
public class UnprocessedRequests {

    private static final ConcurrentHashMap<String, CompletableFuture<RpcResponse<?>>> unprocessedResponseFutures = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<?>> future) {
        unprocessedResponseFutures.put(requestId, future);
    }

    public void remove(String requestId) {
        unprocessedResponseFutures.remove(requestId);
    }

    public void complete(RpcResponse<?> rpcResponse) {
        CompletableFuture<RpcResponse<?>> future = unprocessedResponseFutures.remove(rpcResponse.getRequestId());
        if (future != null) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
