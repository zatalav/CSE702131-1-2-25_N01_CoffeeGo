import { API_ENDPOINTS } from "../config/apiEndpoints.js";
import { apiClient } from "./apiClient.js";

const customerRequest = (endpoint, options = {}, config = {}) =>
  apiClient(endpoint, options, {
    useApiBase: false,
    ...config,
  });

export async function getCustomerCartCount(customerId) {
  return customerRequest(API_ENDPOINTS.customer.cartCount(customerId));
}

export async function getCustomerCartItems(customerId) {
  return customerRequest(API_ENDPOINTS.customer.cartItems(customerId));
}

export async function addCustomerCartItem(payload) {
  return customerRequest(API_ENDPOINTS.customer.cartAdd, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function getCustomerVouchers() {
  return customerRequest(API_ENDPOINTS.customer.cartVouchers);
}

export async function applyCustomerVoucher(payload) {
  return customerRequest(API_ENDPOINTS.customer.cartApplyVoucher, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function updateCustomerCartQty(payload) {
  return customerRequest(API_ENDPOINTS.customer.cartUpdateQty, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function removeCustomerCartItem(payload) {
  return customerRequest(API_ENDPOINTS.customer.cartRemove, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function getCustomerShippingFee(customerId) {
  return customerRequest(API_ENDPOINTS.customer.shippingFee(customerId));
}

export async function checkoutCustomer(payload) {
  return customerRequest(API_ENDPOINTS.customer.checkout, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
