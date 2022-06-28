package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class RpcServer {
    private final ExecutorService threadPool;
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    public RpcServer() {
        // 初始化线程池
        this.threadPool = new ThreadPoolExecutor(10,
                100,
                1,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(100),
                Executors.defaultThreadFactory());
    }

    /**
     * 服务端主动注册服务
     * TODO 修改为注解然后扫描
     */
    public void register(Object service, int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("server starts...");
            Socket socket;
            // Socket[addr=/127.0.0.1,port=65404,localport=9999]
            while ((socket = server.accept()) != null) {
                logger.info("client connected");
                threadPool.execute(new WorkerThread(socket, service));
            }
        } catch (IOException e) {
            logger.error("occur IOException:", e);
        }
    }
}
