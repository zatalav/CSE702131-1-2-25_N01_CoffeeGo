package He_thong_quan_ly.demo.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestPerformanceInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestPerformanceInterceptor.class);
    private static final String START_TIME_ATTR = "requestStartNanos";
    private static final long WARN_THRESHOLD_MS = 500;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        Object startAttr = request.getAttribute(START_TIME_ATTR);
        if (!(startAttr instanceof Long startNs)) {
            return;
        }

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Expose timing to browser devtools for quick diagnosis.
        response.addHeader("Server-Timing", "app;dur=" + elapsedMs);

        if (elapsedMs >= WARN_THRESHOLD_MS) {
            logger.warn("[PERF][SLOW_REQUEST] method={} path={} status={} elapsed={}ms",
                    method,
                    path,
                    response.getStatus(),
                    elapsedMs);
        } else {
            logger.debug("[PERF][REQUEST] method={} path={} status={} elapsed={}ms",
                    method,
                    path,
                    response.getStatus(),
                    elapsedMs);
        }
    }
}
