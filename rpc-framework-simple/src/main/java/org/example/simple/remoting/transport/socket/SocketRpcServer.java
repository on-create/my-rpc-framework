package org.example.simple.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import org.example.common.utils.concurrent.ThreadPoolFactoryUtil;
import org.example.simple.config.CustomShutdownHook;
import org.example.simple.provider.ServiceProvider;
import org.example.simple.provider.impl.ServiceProviderImpl;
import org.example.simple.registry.ServiceRegistry;
import org.example.simple.registry.ZkServiceRegistry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

@Slf4j
public class SocketRpcServer {

    private final ExecutorService threadPool;
    private final String host;
    private final int port;
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        // 初始化线程池
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceRegistry = new ZkServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
    }

    public <T> void publishService(T service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service, serviceClass);
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            server.bind(new InetSocketAddress(host, port));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            // Socket[addr=/127.0.0.1,port=65404,localport=9999]
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }
}
