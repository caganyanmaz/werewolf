package com.caganyanmaz.werewolf.infrastructure;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulingConfig {
    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(2); // enough for your game scale
    }
}