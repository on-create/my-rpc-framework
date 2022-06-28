package org.example.transport.socket;

import lombok.extern.slf4j.Slf4j;
import org.example.factory.SingletonFactory;
import org.example.provider.ServiceProvider;
import org.example.provider.impl.ZkServiceProviderImpl;
import org.example.utils.concurrent.threadpool.ThreadPoolFactoryUtil;

import java.util.concurrent.ExecutorService;

@Slf4j
public class SocketRpcServer {

    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer() {
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }
}
