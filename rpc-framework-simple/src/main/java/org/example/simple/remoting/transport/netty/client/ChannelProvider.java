package org.example.simple.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.common.factory.SingletonFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于获取 Channel 对象
 */
@Slf4j
public final class ChannelProvider {

    private static final Map<String, Channel> channels = new ConcurrentHashMap<>();
    private static final NettyClient nettyClient;

    static {
        nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }

    private ChannelProvider() {

    }

    public static Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // 判断是否有对应地址的连接
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            // 如果有，判断连接是否可用，可用的话就直接获取
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }

        // 否则，重新连接获取 Channel
        Channel channel = nettyClient.doConnect(inetSocketAddress);
        channels.put(key, channel);
        return channel;
    }

    public static void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channels.remove(key);
        log.info("Channel map size :[{}]", channels.size());
    }
}
