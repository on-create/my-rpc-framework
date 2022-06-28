package org.example.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * 创建 ThreadPool（线程池）的工具类
 */
@Slf4j
public final class ThreadPoolFactoryUtil {

    /**
     * 通过threadNamePrefix 来区分不同线程池（我们可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）
     * key: threadNamePrefix
     * value: threadPool
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil() {

    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig config) {
        return createCustomThreadPoolIfAbsent(config, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig config,
                                                                 String threadNamePrefix,
                                                                 Boolean daemon) {
        // 从map中取出 threadNamePrefix 映射的 线程池，如果没有，则创建新的线程池，存入map，并返回
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix,
                key -> createThreadPool(config, threadNamePrefix, daemon));
        // 如果 threadPool 被 shutdown 的话就重新创建一个
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool = createThreadPool(config, threadNamePrefix, daemon);
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    /**
     * shutDown 所有线程池
     */
    public static void shutDownAllThreadPool() {
        log.info("call shutDownAllThreadPool method");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            // 执行shutdown，将会拒绝新任务提交到线程池；待执行的任务不会取消，正在执行的任务也不会取消，将会继续执行直到结束
            executorService.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            // shutdown后等10秒
            try {
                /**
                 * 阻塞当前线程，等已提交和已执行的任务都执行完，解除阻塞
                 * 当等待时间超过设置的时间，检查线程池是否停止，如果停止返回 true， 否则返回false，并解除阻塞
                 */
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
                // 执行shutdownNow，将会拒绝新任务提交到线程池；取消待执行的任务，尝试取消执行中的任务
                executorService.shutdownNow();
            }
        });
    }

    private static ExecutorService createThreadPool(CustomThreadPoolConfig config,
                                                    String threadNamePrefix,
                                                    Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(config.getCorePoolSize(),
                config.getMaximumPoolSize(),
                config.getKeepAliveTime(),
                config.getUnit(),
                config.getWorkQueue(),
                threadFactory);
    }

    /**
     * 创建 ThreadFactory
     * 如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon 指定是否为 Daemon Thread（守护线程）
     * @return Thread Factory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon)
                        .build();
            } else {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * 打印线程池的状态
     * @param threadPool 线程池对象
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                createThreadFactory("print-thread-pool-status", false));

        // 间隔 1秒打印线程池信息
        executor.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
