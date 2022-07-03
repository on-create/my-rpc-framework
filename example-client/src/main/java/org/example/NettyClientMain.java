package org.example;

import org.example.api.Hello;
import org.example.api.HelloService;
import org.example.simple.transport.ClientTransport;
import org.example.simple.proxy.RpcClientProxy;
import org.example.simple.transport.netty.client.NettyClientTransport;

public class NettyClientMain {
    public static void main(String[] args) {
        ClientTransport rpcClient = new NettyClientTransport();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));

        assert "Hello description is 222".equals(hello);
        String hello2 = helloService.hello(new Hello("aaa", "bbb"));
    }
}
