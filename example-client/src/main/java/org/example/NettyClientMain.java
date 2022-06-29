package org.example;

import org.example.api.Hello;
import org.example.api.HelloService;
import org.example.simple.transport.RpcClientProxy;
import org.example.simple.transport.RpcClient;
import org.example.simple.transport.netty.NettyRpcClient;

public class NettyClientMain {
    public static void main(String[] args) {
        RpcClient rpcClient = new NettyRpcClient("127.0.0.1", 9999);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
