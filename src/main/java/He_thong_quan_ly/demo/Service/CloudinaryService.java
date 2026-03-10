package He_thong_quan_ly.demo.Service;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, String> upload(MultipartFile file, String folder) {
        try {
            long startNs = System.nanoTime();

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"));

            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;

            String url = result.get("secure_url").toString();
            String publicId = result.get("public_id").toString();

            logger.info("[PERF][CLOUDINARY_UPLOAD] elapsed={}ms folder={} bytes={} publicId={}",
                    elapsedMs,
                    folder,
                    file.getSize(),
                    publicId);

            return Map.of(
                    "url", url,
                    "publicId", publicId);

        } catch (IOException e) {
            throw new RuntimeException("Upload Cloudinary thất bại", e);
        }
    }

    public void delete(String publicId) {
        try {
            long startNs = System.nanoTime();
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap());
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            logger.info("[PERF][CLOUDINARY_DELETE] elapsed={}ms publicId={}", elapsedMs, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Xóa ảnh Cloudinary thất bại", e);
        }
    }

}
