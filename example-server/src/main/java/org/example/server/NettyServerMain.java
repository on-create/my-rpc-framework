package org.example.server;

import org.example.server.Impl.HelloServiceImpl;
import org.example.simple.registry.DefaultServiceRegistry;
import org.example.simple.transport.netty.server.NettyRpcServer;

public class NettyServerMain {
    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        DefaultServiceRegistry defaultServiceRegistry = new DefaultServiceRegistry();
        // 手动注册
        defaultServiceRegistry.register(helloService);
        NettyRpcServer nettyRpcServer = new NettyRpcServer(9999);
        nettyRpcServer.run();
    }
}
