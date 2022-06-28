package org.example.provider;

import org.example.config.RpcServiceConfig;

public interface ServiceProvider {

    /**
     * @param rpcServiceConfig rpc服务相关属性
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * @param rpcServiceName rpc service name
     * @return service Object
     */
    Object getService(String rpcServiceName);

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
