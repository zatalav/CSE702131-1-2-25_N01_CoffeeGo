package He_thong_quan_ly.demo.Config;

import java.net.URI;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.github.cdimascio.dotenv.Dotenv;

@Component
public class CloudinaryViewConfigResolver {

    private final Environment environment;
    private final Dotenv dotenv;

    public CloudinaryViewConfigResolver(Environment environment) {
        this.environment = environment;
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
    }

    public String resolveCloudName(String cloudNameProperty) {
        String cloudName = firstNonBlank(
                cloudNameProperty,
                environment.getProperty("CLOUDINARY_CLOUD_NAME"),
                environment.getProperty("cloudinary.cloud-name"),
                dotenv.get("CLOUDINARY_CLOUD_NAME"));

        if (!isBlank(cloudName)) {
            return cloudName.trim();
        }

        String cloudinaryUrl = firstNonBlank(
                environment.getProperty("CLOUDINARY_URL"),
                dotenv.get("CLOUDINARY_URL"));

        if (isBlank(cloudinaryUrl)) {
            return "";
        }

        try {
            URI uri = URI.create(cloudinaryUrl.trim());
            String host = uri.getHost();
            return host == null ? "" : host.trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
