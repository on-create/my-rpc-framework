package org.example.common.utils.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.example.common.exception.RpcException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class CuratorUtils {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 5;
    private static final String CONNECTION_STRING = "127.0.0.1:2181";
    public static final String ZK_REGISTER_ROOT_PATH = "/rpc";
    private static final Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();
    private static final Set<String> registeredPathSet = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;

    static {
        zkClient = getZkClient();
    }

    private CuratorUtils() {

    }

    public static CuratorFramework getZkClient() {
        // 重试策略，每次重试时间都比BASE_SLEEP_TIME长
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        CuratorFramework curatorFramework =  CuratorFrameworkFactory.builder()
                // 要连接的服务器（可以是服务器列表）
                .connectString(CONNECTION_STRING)
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.start();
        return curatorFramework;
    }

    /**
     * 创建临时节点
     * 临时节点驻存在Zookeeper中，当连接和session断掉时被删除
     */
    public static void createEphemeralNode(String path) {
        try {
            // 会在nodePath前自动添加workspace的前缀
            if (zkClient.checkExists().forPath(path) == null) {
                zkClient.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path);
                log.info("节点创建成功，节点为:[{}]", path);
            } else {
                log.info("节点已经存在，节点为:[{}]", path);
            }
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 创建持久化节点
     * 持久化节点不会因为客户端断开连接而被删除（需要手动删除）
     * @param path 节点路径
     */
    public static void createPersistentNode(String path) {
        try {
            if (registeredPathSet.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("节点已经存在，节点为:[{}]", path);
            } else {
                // eg: /rpc/org.example.api.HelloService/127.0.0.1:9999
                zkClient.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path);
                log.info("节点创建成功，节点为:[{}]", path);
            }
            registeredPathSet.add(path);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 获取某个字节下的子节点（获取所有提供服务的生产者地址）
     * @param serviceName 服务对象接口名 eg: org.example.api.HelloService
     * @return 指定字节下的所有子节点
     */
    public static List<String> getChildrenNodes(String serviceName) {
        if (serviceAddressMap.containsKey(serviceName)) {
            return serviceAddressMap.get(serviceName);
        }

        List<String> result;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            serviceAddressMap.put(serviceName, result);
            registerWatcher(zkClient, serviceName);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return result;
    }

    /**
     * 注册监听指定节点
     * @param zkClient
     * @param serviceName 服务对象接口名：eg: org.example.api.HelloService
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName) {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = ((curatorFramework, pathChildrenCacheEvent) -> {
            // 子节点更新后触发，更新serviceAddressMap
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            serviceAddressMap.put(serviceName, serviceAddresses);
        });
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 清空注册中心的数据
     */
    public static void clearRegistry() {
        registeredPathSet.stream()
                .parallel()
                .forEach(s -> {
                    try {
                        zkClient.delete().forPath(s);
                    } catch (Exception e) {
                        throw new RpcException(e.getMessage(), e.getCause());
                    }
                });
        log.info("服务端（Provider）所有注册的服务都被清空:[{}]", registeredPathSet.toString());
    }
}
