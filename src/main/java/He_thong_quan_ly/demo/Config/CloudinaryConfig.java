package He_thong_quan_ly.demo.Config;

import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class CloudinaryConfig {

    private final Environment environment;

    public CloudinaryConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public Cloudinary cloudinary() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String cloudinaryUrl = firstNonBlank(
                environment.getProperty("CLOUDINARY_URL"),
                dotenv.get("CLOUDINARY_URL"));

        if (!isBlank(cloudinaryUrl)) {
            return new Cloudinary(cloudinaryUrl);
        }

        String cloudName = firstNonBlank(
                environment.getProperty("CLOUDINARY_CLOUD_NAME"),
                environment.getProperty("cloudinary.cloud-name"),
                dotenv.get("CLOUDINARY_CLOUD_NAME"));

        String apiKey = firstNonBlank(
                environment.getProperty("CLOUDINARY_API_KEY"),
                environment.getProperty("cloudinary.api-key"),
                dotenv.get("CLOUDINARY_API_KEY"));

        String apiSecret = firstNonBlank(
                environment.getProperty("CLOUDINARY_API_SECRET"),
                environment.getProperty("cloudinary.api-secret"),
                dotenv.get("CLOUDINARY_API_SECRET"));

        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            throw new IllegalStateException(
                    "Missing Cloudinary credentials. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET in .env or environment variables.");
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
