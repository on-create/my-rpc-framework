package org.example.simple.registry;

/**
 * 服务注册中心接口
 */
public interface ServiceRegistry {

    <T> void register(T service);

    Object getService(String serviceName);
}
