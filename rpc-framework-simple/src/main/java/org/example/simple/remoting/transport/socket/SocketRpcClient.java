package org.example.simple.remoting.transport.socket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.simple.remoting.dto.RpcRequest;
import org.example.common.exception.RpcException;
import org.example.simple.registry.ServiceDiscovery;
import org.example.simple.registry.ZkServiceDiscovery;
import org.example.simple.remoting.transport.ClientTransport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements ClientTransport {

    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient() {
        this.serviceDiscovery = new ZkServiceDiscovery();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        // 创建Socket对象并且指定服务器的地址和端口号
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            // 对象的序列化流，将指定对象写入ObjectOutputStream
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // 通过输出流向服务器端发送请求信息
            objectOutputStream.writeObject(rpcRequest);
            // 通过输入流获取服务器响应的信息，接收数据前处于阻塞状态
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("调用服务失败:", e);
        }
    }
}
