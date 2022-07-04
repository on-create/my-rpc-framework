package org.example.server;

import org.example.api.HelloService;
import org.example.server.Impl.HelloServiceImpl;
import org.example.simple.remoting.transport.netty.server.NettyServer;

public class NettyServerMain {
    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 9999);
        nettyServer.publishService(helloService, HelloService.class);
    }
}
