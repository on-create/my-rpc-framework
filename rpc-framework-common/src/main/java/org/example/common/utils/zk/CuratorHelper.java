package org.example.common.utils.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.example.common.exception.RpcException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CuratorHelper {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 5;
    private static final String CONNECTION_STRING = "127.0.0.1:2181";
    public static final String ZK_REGISTER_ROOT_PATH = "/rpc";
    private static final Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();
    private static CuratorFramework zkClient = getZkClient();

    private CuratorHelper() {

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
     * 获取某个字节下的子节点（获取所有提供服务的生产者地址）
     */
    public static List<String> getChildrenNodes(String serviceName) {
        if (serviceAddressMap.containsKey(serviceName)) {
            return serviceAddressMap.get(serviceName);
        }

        List<String> result;
        String servicePath = CuratorHelper.ZK_REGISTER_ROOT_PATH + "/" + serviceName;
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
     * 注册监听
     * @param zkClient
     * @param serviceName 服务名称
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName) {
        String servicePath = CuratorHelper.ZK_REGISTER_ROOT_PATH + "/" + serviceName;
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
            log.error("occur exception:", e);
        }
    }
}
