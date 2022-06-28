package org.example.simple;

import org.example.common.dto.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcClientProxy implements InvocationHandler {

    private final String host;
    private final int port;

    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
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
                RpcClientProxy.this  // 代理对象对应的自定义 InvocationHandler
        );
    }

    /**
     * @param proxy 动态生成的代理类
     * @param method 与代理类对象调用的方法相对应
     * @param args 当前method方法的参数
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Builder模式创建对象
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .build();

        RpcClient rpcClient = new RpcClient();
        return rpcClient.sendRpcRequest(rpcRequest, host, port);
    }
}
