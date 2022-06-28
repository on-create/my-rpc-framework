package org.example.simple;

import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.common.enumeration.RpcResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class ClientMessageHandlerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientMessageHandlerThread.class);
    private final Socket socket;
    private final Object service;

    public ClientMessageHandlerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            // 获取客户端发送的请求
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            // 执行请求方法
            Object result = this.invokeTargetMethod(rpcRequest);

            if (result instanceof RpcResponse) {
                // 失败响应
                objectOutputStream.writeObject(result);
            } else {
                objectOutputStream.writeObject(RpcResponse.success(result));
            }
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName(rpcRequest.getInterfaceName());
        // 判断类是否实现了对应的接口
        // class1.isAssignableFrom(class2): class2是不是class1的子类或子接口
        if (!(clazz.isAssignableFrom(service.getClass()))) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_CLASS);
        }

        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            // 没有对应的方法
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}
