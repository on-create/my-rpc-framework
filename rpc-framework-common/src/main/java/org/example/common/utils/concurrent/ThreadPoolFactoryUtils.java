package org.example.common.utils.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.common.utils.concurrent.threadpool.CustomThreadPoolConfig;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建 ThreadPool（线程池）的工具类
 */
@Slf4j
public final class ThreadPoolFactoryUtils {

    private static final Map<String, ExecutorService> threadPools = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtils() {

    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        CustomThreadPoolConfig config = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(config, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        // 如果threadNamePrefix 对应的线程池不存在，则调用createThreadPool方法创建一个，并存入map中
        ExecutorService threadPool = threadPools.computeIfAbsent(threadNamePrefix,
                k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon));
        // 如果 threadPool 被 shutdown的话就重新创建一个
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            threadPool = createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon);
            threadPools.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(
                customThreadPoolConfig.getCorePoolSize(),
                customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(),
                customThreadPoolConfig.getUnit(),
                customThreadPoolConfig.getWorkQueue(),
                threadFactory
        );
    }

    /**
     * 创建 ThreadFactory
     * 如果threadNamePrefix不为空，则使用自建ThreadFactory，否则使用defaultThreadFactory
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon 指定是否为守护线程
     * @return
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon)
                        .build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * shutDown 所有线程池
     */
    public static void shutDownAllThreadPool() {
        log.info("call shutDownAllThreadPool method");
        threadPools.entrySet()
                .parallelStream()
                .forEach(entry -> {
                    ExecutorService executorService = entry.getValue();
                    // 不能再往线程池中添加任何任务,此时线程池不会立刻退出，直到添加到线程池中的任务都已经处理完成，才会退出
                    executorService.shutdown();
                    log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
                    try {
                        executorService.awaitTermination(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        log.error("Thread pool never terminated");
                        // 试图停止所有正在执行的线程，不再处理还在池队列中等待的任务,返回那些未执行的任务
                        executorService.shutdownNow();
                    }
                });
    }
}
