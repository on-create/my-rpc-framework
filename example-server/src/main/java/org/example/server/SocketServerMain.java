package org.example.server;

import org.example.server.Impl.HelloServiceImpl;
import org.example.simple.RpcServer;

public class SocketServerMain {
    public static void main(String[] args) {
        // 创建服务
        /*HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();*/
        // 将服务注册到服务端
        /*rpcServer.register(helloService, 9999);*/

        RpcServer rpcServer = new RpcServer();
        rpcServer.register(new HelloServiceImpl(), 9999);
        // TODO 修改实现方式，通过map存放service解决只能注册一个service
        System.out.println("后面的不会执行");
        rpcServer.register(new HelloServiceImpl(), 9999);
    }
}
