package org.example;

import org.example.api.Hello;
import org.example.api.HelloService;
import org.example.simple.transport.ClientTransport;
import org.example.simple.proxy.RpcClientProxy;
import org.example.simple.transport.socket.SocketRpcClient;

public class RpcFrameworkSimpleClientMain {
    public static void main(String[] args) {
        // 客户端代理
        ClientTransport clientTransport = new SocketRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(clientTransport);
        // 代理对象强制转换为HelloService
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
