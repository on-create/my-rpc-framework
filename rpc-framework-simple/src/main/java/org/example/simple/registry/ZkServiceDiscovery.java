package org.example.simple.registry;

import lombok.extern.slf4j.Slf4j;
import org.example.common.utils.zk.CuratorUtils;

import java.net.InetSocketAddress;

/**
 * 基于 zookeeper 实现服务发现
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // TODO 负载均衡
        // 直接找的第一个地址
        String serviceAddress = CuratorUtils.getChildrenNodes(serviceName).get(0);
        log.info("成功找到服务地址:[{}]", serviceAddress);
        String[] socketAddressArray = serviceAddress.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
