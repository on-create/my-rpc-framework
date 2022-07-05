package org.example;

import org.example.api.Hello;
import org.example.api.HelloService;
import org.example.simple.remoting.transport.ClientTransport;
import org.example.simple.proxy.RpcClientProxy;
import org.example.simple.remoting.transport.netty.client.NettyClientTransport;

public class NettyClientMain {
    public static void main(String[] args) {
        ClientTransport rpcClient = new NettyClientTransport();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));

        assert "Hello description is 222".equals(hello);
        for (int i = 0; i < 50; i++) {
            String tmp = helloService.hello(new Hello("aaa", "~~~" + i));
            System.out.println(tmp);
        }
    }
}
