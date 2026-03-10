package He_thong_quan_ly.demo.auth.service;

import java.time.Duration;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import He_thong_quan_ly.demo.auth.dto.PendingRegisterOtp;

@Service
public class PendingRegisterCacheService {

    private static final int MAX_ENTRIES = 10_000;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final Cache<String, PendingRegisterOtp> pendingCache = Caffeine.newBuilder()
            .maximumSize(MAX_ENTRIES)
            .expireAfterWrite(OTP_TTL)
            .build();

    public void put(String gmail, PendingRegisterOtp pending) {
        String key = normalizeKey(gmail);
        if (key.isEmpty() || pending == null) {
            return;
        }
        pendingCache.put(key, pending);
    }

    public PendingRegisterOtp get(String gmail) {
        String key = normalizeKey(gmail);
        if (key.isEmpty()) {
            return null;
        }
        return pendingCache.getIfPresent(key);
    }

    public void remove(String gmail) {
        String key = normalizeKey(gmail);
        if (key.isEmpty()) {
            return;
        }
        pendingCache.invalidate(key);
    }

    public String normalizeKey(String gmail) {
        if (gmail == null) {
            return "";
        }
        return gmail.trim().toLowerCase(Locale.ROOT);
    }
}


