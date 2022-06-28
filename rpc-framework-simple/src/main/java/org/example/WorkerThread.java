package org.example;

import org.example.dto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class WorkerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);
    private final Socket socket;
    private final Object service;

    public WorkerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        try (ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream())) {
            // 获取客户端发送的请求
            RpcRequest rpcRequest = (RpcRequest) objIn.readObject();
            // 执行请求方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object result = method.invoke(service, rpcRequest.getParameters());
            objOut.writeObject(result);
            objOut.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
