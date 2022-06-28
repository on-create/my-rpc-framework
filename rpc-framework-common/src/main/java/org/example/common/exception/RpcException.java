package org.example.common.exception;

import org.example.common.enumeration.RpcErrorMessageEnum;

public class RpcException extends RuntimeException {

    public RpcException(RpcErrorMessageEnum errorMessageEnum, String detail) {
        super(errorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum errorMessageEnum) {
        super(errorMessageEnum.getMessage());
    }
}
