package com.sloyardms.stashboxapi.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.record-stats:false}")
    private boolean recordStats;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache(CacheNames.USER_ID_BY_PROVIDER_ID, buildCache(
                Duration.ofHours(24), 50_000, recordStats
        ));

        return cacheManager;
    }

    private Cache<Object, Object> buildCache(Duration ttl, long maxSize, boolean recordStats) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize);

        if (recordStats) caffeine.recordStats();

        return caffeine.build();
    }

}
