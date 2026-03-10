package He_thong_quan_ly.demo.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {

    @Bean("cacheManager")
    public CacheManager caffeineCacheManager(
            @Value("${spring.cache.caffeine.spec:maximumSize=5000,expireAfterWrite=3m,recordStats}") String caffeineSpec) {

        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.from(caffeineSpec));
        manager.setAllowNullValues(false);
        return manager;
    }
}
