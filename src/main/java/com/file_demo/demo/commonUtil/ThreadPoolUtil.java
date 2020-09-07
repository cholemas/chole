package com.file_demo.demo.commonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author baichengwei
 * @date 2020/7/17 15:18
 */
public class ThreadPoolUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtil.class);

    /** 线程刷新时间Map */
    private ConcurrentMap<String, Long> threadRefreshTimeMap;
    /** 线程对应的操作对象 */
    private Map<String, Future<?>> threadFutureMap;
    /** 缓存线程池 */
    private ExecutorService cachedThreadPool;
    /** 缓存线程池最后刷新时间 */
    private volatile long                       poolRefreshTime;
    /** 监听线程是否需要监听,一开始默认是需要监听 */
    private volatile boolean                    needMonitorWorkerFlag = true;
    /** 监听池 */
    private ScheduledExecutorService monitorExecutorPool;
    /** 线程超时自动关闭(60秒) */
    private final long                          threadTimeOver = 60000L;
    /** 池子超时自动关闭(60秒) */
    private final long                          poolTimeOver = 60000L;

    /**
     * 提交线程
     */
    public Future<?> submit(Runnable task){
        // 初始化:运行监听线程
        this.runMonitorWorkerTask();
        // 创建线程的唯一id
        String id = UUID.randomUUID().toString();
        // 最新刷新时间
        long timeMillis = System.currentTimeMillis();
        // 更新缓存池刷新时间
        this.poolRefreshTime = timeMillis;
        // 提交线程
        Future<?> future = cachedThreadPool.submit(task);
        // 存储提交的线程刷新时间
        threadRefreshTimeMap.put(id, timeMillis);
        // 存储提交的线程执行对象
        threadFutureMap.put(id, future);
        logger.debug("提交执行线程{}", id);
        return future;
    }

    /**
     * 关闭线程池
     */
    public void shutdown(){
        this.monitorExecutorPool.shutdown();
        this.cachedThreadPool.shutdown();
        logger.debug("关闭线程池");
    }

    /**
     * 创建监控线程
     */
    private Runnable monitorWorker = () -> {
        try {
            logger.debug("监控线程轮询开始, 发现线程池有线程数量:{}", threadRefreshTimeMap.size());
            List<String> removeIdList = new ArrayList<>();
            String threadId;
            for (Map.Entry<String, Long> entry : threadRefreshTimeMap.entrySet()) {
                threadId = entry.getKey();
                if (threadFutureMap.get(threadId).isDone()){
                    // 线程已完成工作,正在待命,newCachedThreadPool机制:如果60秒内无任务,会干掉该线程
                    logger.debug("发现闲时线程,(该线程已完成任务,无需要监听)线程id:{}", threadId);
                    removeIdList.add(threadId);
                }else if (System.currentTimeMillis() - entry.getValue() > threadTimeOver) {
                    // 线程未完成任务,有可能阻塞了,这里我们需要手动干掉该线程,否则有可能是造成资源的浪费
                    logger.debug("发现超时线程,(该线程超时还没完成任务,需要关闭线程)线程id:{}" + threadId);
                    threadFutureMap.get(threadId).cancel(true);
                    removeIdList.add(threadId);
                }
            }
            for (String id : removeIdList) {
                threadFutureMap.remove(id);
                threadRefreshTimeMap.remove(id);
            }
            logger.debug("--监控线程结束, 线程池还剩下线程数量:{}", threadRefreshTimeMap.size());
            if (threadRefreshTimeMap.size() != 0){
                poolRefreshTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - poolRefreshTime > poolTimeOver){
                logger.debug("终止监听池子");
                this.shutdown();
                // 需要监听标志设置为true, 下次运行时开启监听
                needMonitorWorkerFlag = true;
            }
        } catch (Exception e) {
            logger.warn("监听线程池线程出现异常", e);
        }
    };

    /**
     * 开启监控线程
     */
    public void runMonitorWorkerTask() {
        if (needMonitorWorkerFlag){
            synchronized (ThreadPoolUtil.class){
                if(!needMonitorWorkerFlag) {
                    return;
                }

                class DefaultThreadFactory implements ThreadFactory {
                    private final AtomicInteger poolNumber = new AtomicInteger(1);
                    private final ThreadGroup group;
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    private final String namePrefix;

                    DefaultThreadFactory() {
                        SecurityManager s = System.getSecurityManager();
                        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
                        namePrefix = "business-unified-pool-" + poolNumber.getAndIncrement() + "-thread-";
                    }

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
                        if (t.isDaemon()) {
                            t.setDaemon(false);
                        }
                        if (t.getPriority() != Thread.NORM_PRIORITY) {
                            t.setPriority(Thread.NORM_PRIORITY);
                        }
                        return t;
                    }
                }

                threadFutureMap         = new HashMap<>(16);
                threadRefreshTimeMap    = new ConcurrentHashMap<>(16);
                cachedThreadPool        = new ThreadPoolExecutor(50, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
                monitorExecutorPool     = new ScheduledThreadPoolExecutor(1);
                poolRefreshTime         = System.currentTimeMillis();
                monitorExecutorPool.scheduleAtFixedRate(monitorWorker, 0, 1, TimeUnit.SECONDS);
                needMonitorWorkerFlag = false;
            }
        }
    }



}