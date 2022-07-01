package org.example;

import org.example.api.Hello;
import org.example.api.HelloService;
import org.example.simple.transport.ClientTransport;
import org.example.simple.transport.RpcClientProxy;
import org.example.simple.transport.netty.client.NettyClientTransport;

import java.net.InetSocketAddress;

public class NettyClientMain {
    public static void main(String[] args) {
        ClientTransport rpcClient = new NettyClientTransport(new InetSocketAddress("127.0.0.1", 9999));
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));

        System.out.println("上面的调用卡住之后，这里也不会调用了");
        helloService.hello(new Hello("333", "555"));
        System.out.println(hello);
    }
}
