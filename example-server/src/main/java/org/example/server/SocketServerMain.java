package org.example.server;

import org.example.api.HelloService;
import org.example.server.Impl.HelloServiceImpl;
import org.example.simple.transport.socket.SocketRpcServer;

public class SocketServerMain {
    public static void main(String[] args) { ;
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 8080);
        socketRpcServer.publishService(helloService, HelloService.class);
    }
}
