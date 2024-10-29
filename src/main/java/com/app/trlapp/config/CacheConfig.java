package com.app.trlapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setAllowNullValues(false); // Ensure null values are not cached
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(43800, TimeUnit.MINUTES) // Cache entries expire 10 minutes after write
                .maximumSize(500) // Sets the maximum number of cache entries
        );
        return cacheManager;
    }
}
