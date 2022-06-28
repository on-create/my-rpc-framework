package org.example;

import org.example.dto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RpcClient {

    public static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    public Object sendRpcRequest(RpcRequest rpcRequest, String host, int port) {
        // 创建Socket对象并且指定服务器的地址和端口号
        try (Socket socket = new Socket(host, port)) {
            // 对象的序列化流，将指定对象写入ObjectOutputStream
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // 通过输出流向服务器端发送请求信息
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            // 通过输入流获取服务器响应的信息，接收数据前处于阻塞状态
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception:", e);
        }

        return null;
    }
}
