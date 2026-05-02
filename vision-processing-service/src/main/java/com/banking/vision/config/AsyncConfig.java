package com.banking.vision.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for Vision Processing Service.
 * 
 * Provides thread pool for async operations:
 * - Document processing
 * - OCR execution
 * - Image preprocessing
 * - Data extraction
 * 
 * Thread Pool Configuration:
 * - Core pool size: 10
 * - Max pool size: 50
 * - Queue capacity: 100
 * - Thread name prefix: vision-async-
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Task executor for @Async methods.
     * 
     * Configured to prevent thread pool exhaustion while allowing
     * concurrent processing of multiple documents.
     */
    @Bean(name = "visionTaskExecutor")
    public Executor visionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size - always kept alive
        executor.setCorePoolSize(10);
        
        // Maximum pool size - created on demand
        executor.setMaxPoolSize(50);
        
        // Queue capacity - tasks wait here when all threads busy
        executor.setQueueCapacity(100);
        
        // Thread naming for debugging
        executor.setThreadNamePrefix("vision-async-");
        
        // Rejection policy - caller runs (backpressure)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}
