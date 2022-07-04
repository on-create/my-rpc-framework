package org.example.common.utils.checker;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.common.enumeration.RpcErrorMessageEnum;
import org.example.common.enumeration.RpcResponseCode;
import org.example.common.exception.RpcException;

@Slf4j
public final class RpcMessageChecker {

    private static final String INTERFACE_NAME = "interfaceName";

    private RpcMessageChecker() {

    }

    public static void check(RpcRequest rpcRequest, RpcResponse<?> rpcResponse) {
        if (rpcResponse == null) {

            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
