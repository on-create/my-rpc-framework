package org.example.simple.transport.socket;

import org.example.common.utils.concurrent.ThreadPoolFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class SocketRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcServer.class);

    private final ExecutorService threadPool;

    public SocketRpcServer() {
        // 初始化线程池
        threadPool = ThreadPoolFactoryUtil.createDefaultThreadPool("socket-server-rpc-pool");
    }

    public void start(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("server starts...");
            Socket socket;
            // Socket[addr=/127.0.0.1,port=65404,localport=9999]
            while ((socket = server.accept()) != null) {
                logger.info("client connected");
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
        } catch (IOException e) {
            logger.error("occur IOException:", e);
        }
    }
}
