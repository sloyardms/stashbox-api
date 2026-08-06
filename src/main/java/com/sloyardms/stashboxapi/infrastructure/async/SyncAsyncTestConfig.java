package com.sloyardms.stashboxapi.infrastructure.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Profile("test")
public class SyncAsyncTestConfig {

    @Bean(name = "webhookExecutor")
    public Executor webhookExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean(name = "fileCleanupExecutor")
    public Executor fileCleanupExecutor() {
        return new SyncTaskExecutor();
    }
}