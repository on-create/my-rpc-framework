package org.example.simple.transport;

import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.common.enumeration.RpcResponseCode;
import org.example.simple.registry.DefaultServiceRegistry;
import org.example.simple.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RpcRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    private static final ServiceRegistry serviceRegistry;

    static {
        serviceRegistry = new DefaultServiceRegistry();
    }

    /**
     * 处理 rpcRequest 并返回方法执行结果
     * @param rpcRequest
     * @return
     */
    public Object handle(RpcRequest rpcRequest) {
        Object result = null;
        // 通过注册中心获取到目标类
        Object service = serviceRegistry.getService(rpcRequest.getInterfaceName());
        try {
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("service:{} successful invoke method:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("occur org.example.simple.transport.RpcRequestHandler.exception", e);
        }
        return result;
    }

    /**
     * 根据 rpcRequest 和 service 对象特定的方法并返回结果
     * @param rpcRequest
     * @param service
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}
