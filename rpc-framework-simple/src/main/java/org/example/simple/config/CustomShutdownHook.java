package org.example.simple.config;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.example.common.utils.concurrent.ThreadPoolFactoryUtil;
import org.example.common.utils.zk.CuratorUtils;

import java.util.concurrent.ExecutorService;

/**
 * 当服务端（provider）关闭的时候做一些事情，比如取消注册所有服务
 */
@Slf4j
public class CustomShutdownHook {

    private final ExecutorService threadPool = ThreadPoolFactoryUtil.createDefaultThreadPool("custom-shutdown-hook-rpc-pool");
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry();
            threadPool.shutdown();
        }));
    }
}
