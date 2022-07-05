package org.example.simple.config;

import lombok.extern.slf4j.Slf4j;
import org.example.common.utils.concurrent.ThreadPoolFactoryUtils;
import org.example.common.utils.zk.CuratorUtils;

/**
 * 当服务端（provider）关闭的时候做一些事情，比如取消注册所有服务
 */
@Slf4j
public class CustomShutdownHook {

    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        // Runtime.getRuntime.addShutdownHook: 在JVM销毁前执行的一个线程，可以在这个最后执行的线程中把线程池优雅的关闭
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry();
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
