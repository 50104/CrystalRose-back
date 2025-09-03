package com.rose.back.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
}