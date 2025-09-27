package com.rose.back.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {
    
    @Bean(name = "calendarExecutor")
    public Executor calendarExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(5); // 평상시 유지 스레드
        exec.setMaxPoolSize(20); // 최대 스레드
        exec.setQueueCapacity(100); // 대기 큐
        exec.setKeepAliveSeconds(60);
        exec.setThreadNamePrefix("cal-exec-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        exec.initialize();
        return exec;
    }

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // 동시 메일 전송 기본 스레드
        executor.setMaxPoolSize(8);  // 최대 확장
        executor.setQueueCapacity(100); // 대기열
        executor.setThreadNamePrefix("MAIL-ASYNC-");
        executor.setAwaitTerminationSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}