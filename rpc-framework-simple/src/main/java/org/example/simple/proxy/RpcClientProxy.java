package org.example.simple.proxy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.simple.remoting.dto.RpcRequest;
import org.example.simple.remoting.dto.RpcResponse;
import org.example.simple.remoting.transport.ClientTransport;
import org.example.simple.remoting.transport.netty.client.NettyClientTransport;
import org.example.simple.remoting.transport.socket.SocketRpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 动态代理类
 * 当动态代理对象调用一个方法的时候，实际调用的是下面的 invoke 方法
 * 正是因为动态代理才让客户端调用的远程方法像是调用本地方法一样（屏蔽了中间过程）
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    // 用于发送请求给服务端，对应socket 和 netty两种实现方式
    private final ClientTransport clientTransport;

    public RpcClientProxy(ClientTransport clientTransport) {
        this.clientTransport = clientTransport;
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
    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("Call invoke method and invoked method: {}", method.getName());
        // Builder模式创建对象
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .build();

        Object result = null;
        if (clientTransport instanceof NettyClientTransport) {
            CompletableFuture<RpcResponse<?>> completableFuture = (CompletableFuture<RpcResponse<?>>) clientTransport.sendRpcRequest(rpcRequest);
            result = completableFuture.get().getData();
        }

        if (clientTransport instanceof SocketRpcClient) {
            RpcResponse<?> response = (RpcResponse<?>) clientTransport.sendRpcRequest(rpcRequest);
            result = response.getData();
        }
        return result;
    }
}
