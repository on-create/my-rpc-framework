package org.example.simple.transport;

import org.example.common.dto.RpcRequest;

/**
 * 客户端发送 RpcRequest 接口
 */
public interface RpcClient {

    Object sendRpcRequest(RpcRequest rpcRequest);
}
