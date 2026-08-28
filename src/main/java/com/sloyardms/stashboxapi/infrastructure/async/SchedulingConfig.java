package com.sloyardms.stashboxapi.infrastructure.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables {@code @Scheduled} processing for the application.
 * Disabled under the {@code test} profile so cron jobs don't fire during integration tests.
 */
@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig {
}
