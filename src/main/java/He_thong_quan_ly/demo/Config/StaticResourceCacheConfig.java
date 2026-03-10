package He_thong_quan_ly.demo.Config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceCacheConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        CacheControl staticCacheControl = CacheControl.maxAge(365, TimeUnit.DAYS)
                .cachePublic()
                .immutable();

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(staticCacheControl);

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(staticCacheControl);

        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/")
                .setCacheControl(staticCacheControl);

        registry.addResourceHandler("/truycap/**")
                .addResourceLocations("classpath:/static/truycap/")
                .setCacheControl(staticCacheControl);
    }
}
