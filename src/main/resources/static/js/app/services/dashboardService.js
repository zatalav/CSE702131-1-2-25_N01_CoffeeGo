import { apiClient } from "./apiClient.js";
import { API_ENDPOINTS } from "../config/apiEndpoints.js";

export function getDashboardStats() {
  return apiClient(API_ENDPOINTS.dashboard.stats);
}

export function getDashboardOrders() {
  return apiClient(API_ENDPOINTS.dashboard.orders);
}

export function getTopProducts() {
  return apiClient(API_ENDPOINTS.dashboard.topProducts);
}
