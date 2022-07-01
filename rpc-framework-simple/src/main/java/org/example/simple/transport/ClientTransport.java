package org.example.simple.transport;

import org.example.common.dto.RpcRequest;

/**
 * 客户端发送 RpcRequest 接口
 */
public interface ClientTransport {

    /**
     * 发送消息到服务端
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
