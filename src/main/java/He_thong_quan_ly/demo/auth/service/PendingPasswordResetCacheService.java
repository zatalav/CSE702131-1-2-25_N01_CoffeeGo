package He_thong_quan_ly.demo.auth.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import He_thong_quan_ly.demo.auth.dto.PendingPasswordResetOtp;
import He_thong_quan_ly.demo.auth.util.EmailValidatorUtil;

@Service
public class PendingPasswordResetCacheService {

    private static final int MAX_ENTRIES = 10_000;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final Cache<String, PendingPasswordResetOtp> pendingCache = Caffeine.newBuilder()
            .maximumSize(MAX_ENTRIES)
            .expireAfterWrite(OTP_TTL)
            .build();

    public void put(String gmail, PendingPasswordResetOtp pending) {
        String key = normalizeKey(gmail);
        if (key.isEmpty() || pending == null) {
            return;
        }
        pendingCache.put(key, pending);
    }

    public PendingPasswordResetOtp get(String gmail) {
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
        return EmailValidatorUtil.normalize(gmail);
    }
}
