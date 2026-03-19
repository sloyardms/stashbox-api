package com.sloyardms.stashboxapi.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CacheService {

    private final CacheManager cacheManager;

    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        } else {
            log.warn("Attempted to evict from unknown cache: {}", cacheName);
        }
    }

    public void evictAll(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            log.warn("Attempted to clear unknown cache: {}", cacheName);
        }
    }

}
