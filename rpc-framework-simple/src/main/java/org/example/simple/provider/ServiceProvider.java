package org.example.simple.provider;

/**
 * 保存和提供服务实例对象。
 * 服务端使用
 */
public interface ServiceProvider {

    /**
     * 保存服务提供者
     */
    <T> void addServiceProvider(T service);

    /**
     * 获得服务提供者
     */
    Object getServiceProvider(String serviceName);
}
