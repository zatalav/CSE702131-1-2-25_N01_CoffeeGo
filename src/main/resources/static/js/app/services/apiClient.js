import { API_BASE_URL } from "../config/apiEndpoints.js";
import { REQUEST_TIMEOUT_MS } from "../config/constants.js";

export async function apiClient(endpoint, options = {}, config = {}) {
  const { useApiBase = true, responseType = "json" } = config;
  const url = useApiBase ? `${API_BASE_URL}${endpoint}` : endpoint;
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  try {
    const response = await fetch(url, {
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {}),
      },
      ...options,
      signal: controller.signal,
    });

    if (!response.ok) {
      const errorText = await response.text().catch(() => "");
      const message =
        errorText || `API error: ${response.status} ${response.statusText}`;
      const error = new Error(message);
      error.status = response.status;
      throw error;
    }

    if (responseType === "raw") return response;
    if (responseType === "text") return response.text();
    return response.status === 204 ? null : response.json();
  } finally {
    clearTimeout(timeoutId);
  }
}
