package org.example.simple.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.example.common.enumeration.RpcErrorMessageEnum;
import org.example.common.exception.RpcException;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 用于获取 Channel 对象
 */
@Slf4j
public class ChannelProvider {

    private static final Bootstrap bootstrap = NettyClient.initializeBootstrap();
    private static Channel channel = null;

    /**
     * 最多重试次数
     */
    private static final int MAX_RETRY_COUNT = 5;

    public static Channel get(InetSocketAddress inetSocketAddress) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            connect(inetSocketAddress, countDownLatch);
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("occur exception when get  channel:", e);
        }
        return channel;
    }

    private static void connect(InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch) {
        connect(ChannelProvider.bootstrap, inetSocketAddress, MAX_RETRY_COUNT, countDownLatch);
    }

    /**
     * 带有重试机制的客户端连接方法
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, int retry, CountDownLatch countDownLatch) {
        bootstrap.connect(inetSocketAddress)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("客户端连接成功!");
                        channel = future.channel();
                        countDownLatch.countDown();
                        return;
                    }

                    if (retry == 0) {
                        log.error("客户端连接失败:重试次数已用完，放弃连接!");
                        countDownLatch.countDown();
                        throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE);
                    }

                    // 第 n 次重连
                    int order = (MAX_RETRY_COUNT - retry) + 1;
                    // 本次重连的间隔
                    int delay = 1 << order;
                    log.error("{}: 连接失败，第 {} 次重连……", new Date(), order);
                    bootstrap.config()
                            .group()
                            .schedule(() -> connect(bootstrap, inetSocketAddress, retry - 1, countDownLatch),
                                    delay, TimeUnit.SECONDS);
                });
    }
}
