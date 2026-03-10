import { apiClient } from "./apiClient.js";
import { API_ENDPOINTS } from "../config/apiEndpoints.js";

export function getProducts() {
  return apiClient(API_ENDPOINTS.products.list);
}

export function getProductById(id) {
  return apiClient(API_ENDPOINTS.products.detail(id));
}
