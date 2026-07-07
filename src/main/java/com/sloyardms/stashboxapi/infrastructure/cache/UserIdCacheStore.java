package com.sloyardms.stashboxapi.infrastructure.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

@Component
public class UserIdCacheStore {

    private static final String CACHE_NAME = "userIdByExternalId";

    private final Cache cache;

    public UserIdCacheStore(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(CACHE_NAME);
        if (this.cache == null) {
            throw new IllegalStateException(
                    "Cache '" + CACHE_NAME + "' not found — check spring.cache.cache-names");
        }
    }

    public UUID getOrLoad(UUID externalId, Callable<UUID> loader) {
        return cache.get(externalId, loader);
    }

    public Optional<UUID> get(UUID externalId) {
        Cache.ValueWrapper wrapper = cache.get(externalId);
        return Optional.ofNullable(wrapper).map(w -> (UUID) w.get());
    }

    public void put(UUID externalId, UUID userId) {
        cache.put(externalId, userId);
    }

    public void evict(UUID externalId) {
        cache.evict(externalId);
    }

}
