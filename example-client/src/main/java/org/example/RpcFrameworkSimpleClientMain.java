package org.example;

import org.example.api.Hello;
import org.example.api.HelloService;
import org.example.simple.remoting.socket.RpcClientProxy;

public class RpcFrameworkSimpleClientMain {
    public static void main(String[] args) {
        // 客户端代理
        RpcClientProxy rpcClientProxy = new RpcClientProxy("127.0.0.1", 9999);
        // 代理对象强制转换为HelloService
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
