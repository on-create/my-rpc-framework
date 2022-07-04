package org.example.simple.provider.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.common.enumeration.RpcErrorMessageEnum;
import org.example.common.exception.RpcException;
import org.example.simple.provider.ServiceProvider;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现了 ServiceProvider接口，可以看做一个保存和提供服务实例对象的示例
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * 接口名和服务的对应关系
     * note: 处理一个接口被两个实现类实现的情况如何处理？(通过group分组)
     * key: service/interface name
     * value: service
     */
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * note: 可以修改为扫描注解注册
     * 将这个对象所有实现的接口都注册进去
     */
    @Override
    public <T> void addServiceProvider(T service, Class<T> serviceClass) {
        /**
         * getCanonicalName(): 获取该类的规范名称
         * String[] strings = new String[]{"a", "b"}
         * getName() --> [Ljava.lang.String;
         * getCanonicalName() --> java.lang.String[]
          */
        String serviceName = serviceClass.getCanonicalName();
        // 如果该类已注册，直接返回
        if (registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        serviceMap.put(serviceName, service);
        log.info("Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}
