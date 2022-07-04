package org.example.simple.registry;

import lombok.extern.slf4j.Slf4j;
import org.example.common.utils.zk.CuratorHelper;

import java.net.InetSocketAddress;

/**
 * 基于 zookeeper 实现服务注册
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        // 根节点下注册子节点：服务
        StringBuilder servicePath = new StringBuilder(CuratorHelper.ZK_REGISTER_ROOT_PATH)
                .append("/")
                .append(serviceName);
        // 服务子节点下注册子节点：服务地址
        // inetSocketAddress("127.0.0.1", 9333).toString() -> /127.0.0.1:9333
        servicePath.append(inetSocketAddress.toString());
        CuratorHelper.createEphemeralNode(servicePath.toString());
    }
}
