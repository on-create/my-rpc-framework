package org.example.simple.transport.socket;

import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.simple.transport.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketRpcRequestHandlerRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcRequestHandlerRunnable.class);
    private final Socket socket;
    private static final RpcRequestHandler requestHandler;

    static {
        requestHandler = new RpcRequestHandler();
    }

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        logger.info(String.format("server handle message from client by thread: %s", Thread.currentThread().getName()));
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            // 获取请求
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            // 执行方法
            Object result = requestHandler.handle(rpcRequest);
            // 将结果注入ObjectOutputStream
            if (result instanceof RpcResponse) {
                objectOutputStream.writeObject(result);
            } else {
                objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            }
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur org.example.simple.transport.socket.SocketRpcRequestHandlerRunnable.exception:", e);
        }
    }
}
