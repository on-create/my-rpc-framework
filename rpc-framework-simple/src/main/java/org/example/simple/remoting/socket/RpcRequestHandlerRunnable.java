package org.example.simple.remoting.socket;

import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.simple.registry.ServiceRegistry;
import org.example.simple.remoting.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RpcRequestHandlerRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandlerRunnable.class);
    private final Socket socket;
    private final RpcRequestHandler requestHandler;
    private final ServiceRegistry serviceRegistry;

    public RpcRequestHandlerRunnable(Socket socket, RpcRequestHandler requestHandler, ServiceRegistry serviceRegistry) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            // 获取请求
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            // 请求方法所属接口
            String interfaceName = rpcRequest.getInterfaceName();
            // 根据接口名获取对应的服务对象
            Object service = serviceRegistry.getService(interfaceName);
            // 执行方法
            Object result = requestHandler.handle(rpcRequest, service);
            // 将结果注入ObjectOutputStream
            if (result instanceof RpcResponse) {
                objectOutputStream.writeObject(result);
            } else {
                objectOutputStream.writeObject(RpcResponse.success(result));
            }
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception:", e);
        }
    }
}
