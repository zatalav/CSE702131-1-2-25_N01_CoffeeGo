package He_thong_quan_ly.demo.Service.customer.checkout;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

@Service
public class GoogleDistanceClient {

    private static final Logger logger = LoggerFactory.getLogger(GoogleDistanceClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    public boolean isConfigured() {
        return googleMapsApiKey != null && !googleMapsApiKey.isBlank();
    }

    public Double fetchDistanceKm(String originAddress, String destinationAddress) {
        if (!isConfigured()) {
            return null;
        }
        try {
            String origin = URLEncoder.encode(originAddress, StandardCharsets.UTF_8);
            String destination = URLEncoder.encode(destinationAddress, StandardCharsets.UTF_8);
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
                    + origin
                    + "&destinations="
                    + destination
                    + "&mode=driving&language=vi&key="
                    + googleMapsApiKey;

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return null;
            }

            String apiStatus = String.valueOf(response.getOrDefault("status", ""));
            if (!"OK".equalsIgnoreCase(apiStatus)) {
                return null;
            }

            Object rowsObj = response.get("rows");
            if (!(rowsObj instanceof List<?> rows) || rows.isEmpty()) {
                return null;
            }
            Object rowObj = rows.get(0);
            if (!(rowObj instanceof Map<?, ?> rowMap)) {
                return null;
            }
            Object elementsObj = rowMap.get("elements");
            if (!(elementsObj instanceof List<?> elements) || elements.isEmpty()) {
                return null;
            }
            Object elementObj = elements.get(0);
            if (!(elementObj instanceof Map<?, ?> elementMap)) {
                return null;
            }
            Object statusObj = elementMap.get("status");
            if (statusObj == null || !"OK".equalsIgnoreCase(String.valueOf(statusObj))) {
                return null;
            }

            Object distanceObj = elementMap.get("distance");
            if (!(distanceObj instanceof Map<?, ?> distanceMap)) {
                return null;
            }
            Object valueObj = distanceMap.get("value");
            if (!(valueObj instanceof Number meters)) {
                return null;
            }

            return meters.doubleValue() / 1000.0;
        } catch (RuntimeException ex) {
            logger.warn("Google Distance Matrix call failed. origin={}, destination={}, error={}",
                    originAddress,
                    destinationAddress,
                    ex.getMessage());
            return null;
        }
    }

    public Map<String, Object> buildDiagnostic(String originAddress, String destinationAddress) {
        Map<String, Object> diagnostic = new LinkedHashMap<>();
        diagnostic.put("ok", false);
        diagnostic.put("distanceKm", null);
        diagnostic.put("apiStatus", "");
        diagnostic.put("elementStatus", "");
        diagnostic.put("errorMessage", "");

        if (!isConfigured()) {
            diagnostic.put("errorMessage", "Thieu google.maps.api.key");
            return diagnostic;
        }

        try {
            String origin = URLEncoder.encode(originAddress, StandardCharsets.UTF_8);
            String destination = URLEncoder.encode(destinationAddress, StandardCharsets.UTF_8);
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
                    + origin
                    + "&destinations="
                    + destination
                    + "&mode=driving&language=vi&key="
                    + googleMapsApiKey;

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                diagnostic.put("errorMessage", "Google tra ve rong");
                return diagnostic;
            }

            String apiStatus = String.valueOf(response.getOrDefault("status", ""));
            diagnostic.put("apiStatus", apiStatus);
            Object errorMessage = response.get("error_message");
            if (errorMessage != null) {
                diagnostic.put("errorMessage", String.valueOf(errorMessage));
            }

            Object rowsObj = response.get("rows");
            if (!(rowsObj instanceof List<?> rows) || rows.isEmpty()) {
                return diagnostic;
            }
            Object rowObj = rows.get(0);
            if (!(rowObj instanceof Map<?, ?> rowMap)) {
                return diagnostic;
            }
            Object elementsObj = rowMap.get("elements");
            if (!(elementsObj instanceof List<?> elements) || elements.isEmpty()) {
                return diagnostic;
            }
            Object elementObj = elements.get(0);
            if (!(elementObj instanceof Map<?, ?> elementMap)) {
                return diagnostic;
            }

            Object elementStatusObj = elementMap.get("status");
            String elementStatus = elementStatusObj == null ? "" : String.valueOf(elementStatusObj);
            diagnostic.put("elementStatus", elementStatus);
            if (!"OK".equalsIgnoreCase(elementStatus)) {
                if (((String) diagnostic.get("errorMessage")).isBlank()) {
                    diagnostic.put("errorMessage", "Element status: " + elementStatus);
                }
                return diagnostic;
            }

            Object distanceObj = elementMap.get("distance");
            if (!(distanceObj instanceof Map<?, ?> distanceMap)) {
                return diagnostic;
            }
            Object valueObj = distanceMap.get("value");
            if (!(valueObj instanceof Number meters)) {
                return diagnostic;
            }

            double km = meters.doubleValue() / 1000.0;
            diagnostic.put("ok", true);
            diagnostic.put("distanceKm", km);
            return diagnostic;
        } catch (RuntimeException ex) {
            diagnostic.put("errorMessage", ex.getMessage() == null ? "Loi goi Google API" : ex.getMessage());
            return diagnostic;
        }
    }
}
