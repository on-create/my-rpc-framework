package org.example.simple.remoting.dto;

import lombok.*;
import org.example.common.enumeration.RpcMessageTypeEnum;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    private String requestId;

    // 待调用接口名称
    private String interfaceName;

    // 待调用方法名称
    private String methodName;

    // 调用方法的参数
    private Object[] parameters;

    // 调用方法的参数类型
    private Class<?>[] paramTypes;

    private RpcMessageTypeEnum rpcMessageTypeEnum;
}
