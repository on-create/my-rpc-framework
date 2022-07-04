package org.example.simple.transport.socket;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.common.utils.factory.SingletonFactory;
import org.example.simple.handler.RpcRequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable {

    private final Socket socket;
    private final RpcRequestHandler requestHandler;

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
        this.requestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
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
            log.error("occur org.example.simple.transport.socket.SocketRpcRequestHandlerRunnable.exception:", e);
        }
    }
}
