package org.example.simple;

import org.example.common.enumeration.RpcErrorMessageEnum;
import org.example.common.exception.RpcException;
import org.example.simple.registry.ServiceRegistry;
import org.example.simple.remoting.RpcRequestHandler;
import org.example.simple.remoting.socket.RpcRequestHandlerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    /**
     * 线程池参数
     */
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE = 100;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    private final ExecutorService threadPool;
    private final RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();
    private final ServiceRegistry serviceRegistry;

    public RpcServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;

        // 初始化线程池
        this.threadPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY),
                Executors.defaultThreadFactory());
    }

    public void start(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("server starts...");
            Socket socket;
            // Socket[addr=/127.0.0.1,port=65404,localport=9999]
            while ((socket = server.accept()) != null) {
                logger.info("client connected");
                threadPool.execute(new RpcRequestHandlerRunnable(socket, rpcRequestHandler, serviceRegistry));
            }
        } catch (IOException e) {
            logger.error("occur IOException:", e);
        }
    }
}
