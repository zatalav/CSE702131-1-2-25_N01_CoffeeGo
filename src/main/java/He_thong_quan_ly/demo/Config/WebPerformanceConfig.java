package He_thong_quan_ly.demo.Config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebPerformanceConfig implements WebMvcConfigurer {

    private final RequestPerformanceInterceptor requestPerformanceInterceptor;
    private final StringToLocalDateConverter stringToLocalDateConverter;

    public WebPerformanceConfig(
            RequestPerformanceInterceptor requestPerformanceInterceptor,
            StringToLocalDateConverter stringToLocalDateConverter) {
        this.requestPerformanceInterceptor = requestPerformanceInterceptor;
        this.stringToLocalDateConverter = stringToLocalDateConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLocalDateConverter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestPerformanceInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/img_kh/**", "/img_nv/**", "/favicon.ico");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        CacheControl staticCache = CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic();

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(staticCache);

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(staticCache);

        registry.addResourceHandler("/img/**", "/img_kh/**", "/img_nv/**")
                .addResourceLocations("classpath:/static/img/", "classpath:/static/img_kh/",
                        "classpath:/static/img_nv/")
                .setCacheControl(staticCache);
    }
}
