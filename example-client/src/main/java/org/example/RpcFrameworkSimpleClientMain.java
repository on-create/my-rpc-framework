package org.example;

import org.example.api.Hello;
import org.example.api.HelloService;
import org.example.simple.transport.RpcClientProxy;
import org.example.simple.transport.RpcClient;
import org.example.simple.transport.netty.client.NettyRpcClient;
import org.example.simple.transport.socket.SocketRpcClient;

public class RpcFrameworkSimpleClientMain {
    public static void main(String[] args) {
        // 客户端代理
        //RpcClientProxy rpcClientProxy = new RpcClientProxy("127.0.0.1", 9999);
        RpcClient rpcClient = new SocketRpcClient("127.0.0.1", 9999);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        // 代理对象强制转换为HelloService
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);

        RpcClient rpcClient2 = new NettyRpcClient("127.0.0.1", 9999);
    }
}
