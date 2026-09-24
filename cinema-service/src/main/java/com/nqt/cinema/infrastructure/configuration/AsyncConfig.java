package com.nqt.cinema.infrastructure.configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // 1 luồng duy nhất: vừa đóng vai trò hàng đợi render tạm thời, vừa tránh nhiều job
    // FFmpeg chạy song song làm quá tải VPS. Tăng lên sau theo kế hoạch queue toàn hệ thống.
    @Bean(name = "clipRenderExecutor")
    public Executor clipRenderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("clip-render-");
        executor.initialize();
        return executor;
    }
}