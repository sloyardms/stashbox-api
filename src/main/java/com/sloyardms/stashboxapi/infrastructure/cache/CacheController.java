package com.sloyardms.stashboxapi.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/cache/stats")
public class CacheController {

    private CacheManager cacheManager;

    @GetMapping
    public List<CacheInfo> getCacheInfo() {
        return cacheManager.getCacheNames()
                .stream()
                .map(this::getCacheInfo)
                .toList();
    }

    private CacheInfo getCacheInfo(String cacheName) {
        Cache<Object, Object> cache = (Cache) cacheManager.getCache(cacheName).getNativeCache();
        Set<Object> keys = cache.asMap().keySet();
        CacheStats stats = cache.stats();
        return new CacheInfo(
                cacheName, keys.size(), keys, stats.toString());
    }

    private record CacheInfo(String name, int size, Set<Object> keys, String stats) {
    }

}
