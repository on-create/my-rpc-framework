package org.example.simple.transport.socket;

import lombok.AllArgsConstructor;
import org.example.common.dto.RpcRequest;
import org.example.common.dto.RpcResponse;
import org.example.common.enumeration.RpcErrorMessageEnum;
import org.example.common.enumeration.RpcResponseCode;
import org.example.common.exception.RpcException;
import org.example.common.utils.checker.RpcMessageChecker;
import org.example.simple.transport.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@AllArgsConstructor
public class SocketRpcClient implements RpcClient {

    public static final Logger logger = LoggerFactory.getLogger(SocketRpcClient.class);
    private final String host;
    private final int port;

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 创建Socket对象并且指定服务器的地址和端口号
        try (Socket socket = new Socket(host, port)) {
            // 对象的序列化流，将指定对象写入ObjectOutputStream
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // 通过输出流向服务器端发送请求信息
            objectOutputStream.writeObject(rpcRequest);
            // 通过输入流获取服务器响应的信息，接收数据前处于阻塞状态
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            RpcResponse<?> rpcResponse = (RpcResponse<?>) objectInputStream.readObject();

            // 校验 RpcResponse 和 RpcRequest
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception:", e);
        }

        return null;
    }
}
