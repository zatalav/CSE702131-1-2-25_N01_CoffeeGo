package He_thong_quan_ly.demo.Service.customer.checkout;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NominatimGeocoder {

    private static final Logger logger = LoggerFactory.getLogger(NominatimGeocoder.class);
    private static final ReentrantLock NOMINATIM_RATE_LOCK = new ReentrantLock();
    private static volatile long lastNominatimRequestAtMs = 0L;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, double[]> geocodeCache = new ConcurrentHashMap<>();

    public Double estimateDistanceKmByAddress(String originAddress, String destinationAddress) {
        try {
            double[] originLatLon = approximateCoordinateFromAddress(originAddress);
            double[] destinationLatLon = approximateCoordinateFromAddress(destinationAddress);

            if (originLatLon == null) {
                originLatLon = geocodeAddressByNominatim(originAddress);
            }
            if (destinationLatLon == null) {
                destinationLatLon = geocodeAddressByNominatim(destinationAddress);
            }

            if (originLatLon == null || destinationLatLon == null) {
                return null;
            }

            double km = haversineKm(
                    originLatLon[0],
                    originLatLon[1],
                    destinationLatLon[0],
                    destinationLatLon[1]);

            if (Double.isNaN(km) || Double.isInfinite(km) || km <= 0) {
                return null;
            }

            return km;
        } catch (RuntimeException ex) {
            logger.warn("Shipping fallback distance failed. origin={}, destination={}, error={}",
                    originAddress,
                    destinationAddress,
                    ex.getMessage());
            return null;
        }
    }

    private double[] geocodeAddressByNominatim(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        String fixedAddress = tryFixMojibake(address);
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(fixedAddress.trim());

        String noDiacritics = Normalizer
                .normalize(fixedAddress, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('\u0111', 'd')
                .replace('\u0110', 'D')
                .trim();
        if (!noDiacritics.isBlank()) {
            candidates.add(noDiacritics);
        }

        String[] parts = fixedAddress.split(",");
        if (parts.length >= 2) {
            StringBuilder withoutFirst = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (withoutFirst.length() > 0) {
                    withoutFirst.append(", ");
                }
                withoutFirst.append(parts[i].trim());
            }
            if (withoutFirst.length() > 0) {
                candidates.add(withoutFirst.toString());
                String noDiacriticsShort = Normalizer
                        .normalize(withoutFirst.toString(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .replace('\u0111', 'd')
                        .replace('\u0110', 'D')
                        .trim();
                if (!noDiacriticsShort.isBlank()) {
                    candidates.add(noDiacriticsShort);
                }
            }
        }

        LinkedHashSet<String> expandedCandidates = new LinkedHashSet<>();
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            expandedCandidates.add(candidate);
            if (!candidate.toLowerCase(Locale.ROOT).contains("vietnam")) {
                expandedCandidates.add(candidate + ", Vietnam");
            }
        }

        for (String candidate : expandedCandidates) {
            String cacheKey = normalizeAddressKey(candidate);
            double[] cached = geocodeCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        for (String candidate : expandedCandidates) {
            double[] latLon = geocodeSingleQuery(candidate);
            if (latLon != null) {
                geocodeCache.put(normalizeAddressKey(candidate), latLon);
                geocodeCache.put(normalizeAddressKey(address), latLon);
                return latLon;
            }
        }

        return null;
    }

    private double[] geocodeSingleQuery(String queryAddress) {
        waitForNominatimRateLimit();

        String query = URLEncoder.encode(queryAddress, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + query;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "QuanCoffee/1.0 (shipping-fallback)");
        headers.set("Accept", "application/json");

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                });

        List<Map<String, Object>> body = response.getBody();
        if (body == null || body.isEmpty()) {
            return null;
        }

        Map<String, Object> map = body.get(0);

        Object latObj = map.get("lat");
        Object lonObj = map.get("lon");
        if (latObj == null || lonObj == null) {
            return null;
        }

        double lat = Double.parseDouble(String.valueOf(latObj));
        double lon = Double.parseDouble(String.valueOf(lonObj));
        return new double[] { lat, lon };
    }

    private void waitForNominatimRateLimit() {
        NOMINATIM_RATE_LOCK.lock();
        try {
            long now = System.currentTimeMillis();
            long elapsed = now - lastNominatimRequestAtMs;
            long minIntervalMs = 1100L;
            if (elapsed < minIntervalMs) {
                try {
                    Thread.sleep(minIntervalMs - elapsed);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            lastNominatimRequestAtMs = System.currentTimeMillis();
        } finally {
            NOMINATIM_RATE_LOCK.unlock();
        }
    }

    private String normalizeAddressKey(String address) {
        if (address == null) {
            return "";
        }
        return address.trim().toLowerCase(Locale.ROOT);
    }

    private String tryFixMojibake(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        if (!(text.contains("Ãƒ") || text.contains("Ã‚") || text.contains("Ã¡Â»"))) {
            return text;
        }
        try {
            String recovered = new String(text.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            if (!recovered.isBlank()) {
                return recovered;
            }
        } catch (RuntimeException ignored) {
        }
        return text;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private double[] approximateCoordinateFromAddress(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String text = Normalizer.normalize(tryFixMojibake(address), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('\u0111', 'd')
                .replace('\u0110', 'D')
                .toLowerCase(Locale.ROOT);

        if (text.contains("ha dong")) {
            return new double[] { 20.958, 105.756 };
        }
        if (text.contains("dong da")) {
            return new double[] { 21.017, 105.827 };
        }
        if (text.contains("thanh xuan")) {
            return new double[] { 20.996, 105.809 };
        }
        if (text.contains("cau giay")) {
            return new double[] { 21.036, 105.790 };
        }
        if (text.contains("ba dinh")) {
            return new double[] { 21.033, 105.814 };
        }
        if (text.contains("hoan kiem")) {
            return new double[] { 21.028, 105.852 };
        }
        return null;
    }
}
