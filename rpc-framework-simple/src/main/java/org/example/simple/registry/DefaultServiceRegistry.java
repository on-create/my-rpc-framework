package org.example.simple.registry;

import org.example.common.enumeration.RpcErrorMessageEnum;
import org.example.common.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);

    /**
     * 接口名和服务的对应关系，TODO 处理一个接口被两个实现类实现的情况
     * key: service/interface name
     * value: service
     */
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * TODO 修改为扫描注解注册
     * 将这个对象所有实现的接口都注册进去
     */
    @Override
    public synchronized <T> void register(T service) {
        /**
         * getCanonicalName(): 获取该类的规范名称
         * String[] strings = new String[]{"a", "b"}
         * getName() --> [Ljava.lang.String;
         * getCanonicalName() --> java.lang.String[]
          */
        String serviceName = service.getClass().getCanonicalName();
        // 如果该类已注册，直接返回
        if (registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            // service 没有实现任何接口
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }

        // 不能解决同一个接口被两个实现类实现的问题
        for (Class<?> anInterface : interfaces) {
            serviceMap.put(anInterface.getCanonicalName(), service);
        }
        logger.info("Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public synchronized Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}
