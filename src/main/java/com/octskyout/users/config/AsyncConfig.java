package com.octskyout.users.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();

        threadPool.setCorePoolSize(2);
        threadPool.setMaxPoolSize(2);
        threadPool.setQueueCapacity(5);
        threadPool.setAllowCoreThreadTimeOut(true);
        threadPool.setPrestartAllCoreThreads(false);
        threadPool.setThreadNamePrefix("SCHEDULE-THREAD");
        threadPool.initialize();

        return threadPool;
    }
}
