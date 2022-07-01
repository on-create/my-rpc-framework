package org.example.simple.transport;

import org.example.common.dto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 动态代理类
 * 当动态代理对象调用一个方法的时候，实际调用的是下面的 invoke 方法
 */
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    // 用于发送请求给服务端，对应socket 和 netty两种实现方式
    private final RpcClient rpcClient;

    public RpcClientProxy(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        /**
         * newProxyInstance(): 返回指定接口的代理类实例，该接口将方法调用分派给指定的调用处理程序
         * 参数 interfaces 含义：
         * 一个Interface对象的数组，表示的是我将要给我需要代理的对象提供一组什么接口，
         * 如果我提供了一组接口给它，那么这个代理对象就宣称实现了该接口(多态)，这样我就能调用这组接口中的方法了
         */
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(), // 代理类的类加载器
                new Class<?>[]{clazz},  // 代理类要实现的接口列表
                this  // 代理对象对应的自定义 InvocationHandler
        );
    }

    /**
     * @param proxy 动态生成的代理类
     * @param method 与代理类对象调用的方法相对应
     * @param args 当前method方法的参数
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.info("Call invoke method and invoked method: {}", method.getName());
        // Builder模式创建对象
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .build();

        return rpcClient.sendRpcRequest(rpcRequest);
    }
}
