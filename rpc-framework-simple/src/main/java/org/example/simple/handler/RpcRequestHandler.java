package org.example.simple.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.simple.remoting.dto.RpcRequest;
import org.example.simple.remoting.dto.RpcResponse;
import org.example.common.enumeration.RpcResponseCode;
import org.example.common.exception.RpcException;
import org.example.simple.provider.ServiceProvider;
import org.example.simple.provider.impl.ServiceProviderImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class RpcRequestHandler {

    private static final ServiceProvider serviceProvider = new ServiceProviderImpl();

    /**
     * 处理 rpcRequest: 调用对应的方法，并返回方法执行结果
     * @param rpcRequest
     * @return
     */
    public Object handle(RpcRequest rpcRequest) {
        // 通过注册中心获取到目标类
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 根据 rpcRequest 和 service 对象特定的方法并返回结果
     * @param rpcRequest 客户端请求
     * @param service 提供服务的对象
     * @return 目标方法执行的结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        /*Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }
        return method.invoke(service, rpcRequest.getParameters());*/

        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
