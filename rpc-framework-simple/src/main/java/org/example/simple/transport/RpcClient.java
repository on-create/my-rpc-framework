package org.example.simple.transport;

import org.example.common.dto.RpcRequest;

public interface RpcClient {

    Object sendRpcRequest(RpcRequest rpcRequest);
}
