package com.banking.document.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int queueCapacity;
    private final String threadNamePrefix;

    public AsyncConfig(
            @Value("${async.core-pool-size:10}") int corePoolSize,
            @Value("${async.max-pool-size:50}") int maxPoolSize,
            @Value("${async.queue-capacity:100}") int queueCapacity,
            @Value("${async.thread-name-prefix:async-document-}") String threadNamePrefix) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueCapacity = queueCapacity;
        this.threadNamePrefix = threadNamePrefix;
    }

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
