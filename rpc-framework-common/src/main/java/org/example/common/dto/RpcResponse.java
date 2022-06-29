package org.example.common.dto;

import lombok.*;
import org.example.common.enumeration.RpcResponseCode;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 175745410605631233L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     * @param data  返回数据
     * @param <T>   返回结果类型
     * @return 响应成功
     */
    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCode.SUCCESS.getCode());
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    /**
     * 响应失败
     * @param rpcResponseCode 响应状态码信息
     * @param <T>   返回类型
     * @return 响应失败结果
     */
    public static <T> RpcResponse<T> fail(RpcResponseCode rpcResponseCode) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCode.getCode());
        response.setMessage(rpcResponseCode.getMessage());
        return response;
    }
}
