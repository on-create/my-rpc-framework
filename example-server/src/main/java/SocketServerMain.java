import org.example.HelloService;
import org.example.Impl.HelloServiceImpl;
import org.example.RpcServer;

public class SocketServerMain {
    public static void main(String[] args) {
        // 创建服务
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        // 将服务注册到服务端
        rpcServer.register(helloService, 9999);
    }
}
