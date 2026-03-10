export const API_BASE_URL = "http://localhost:8080/api";

export const API_ENDPOINTS = {
  customer: {
    cartCount: (customerId) =>
      `/customer/cart/count?customerId=${encodeURIComponent(customerId)}`,
    cartItems: (customerId) =>
      `/customer/cart/items?customerId=${encodeURIComponent(customerId)}`,
    cartAdd: "/customer/cart/add",
    cartUpdateQty: "/customer/cart/update-qty",
    cartRemove: "/customer/cart/remove",
    cartVouchers: "/customer/cart/vouchers",
    cartApplyVoucher: "/customer/cart/apply-voucher",
    shippingFee: (customerId) =>
      `/customer/shipping-fee?customerId=${encodeURIComponent(customerId)}`,
    checkout: "/customer/checkout",
  },
  dashboard: {
    stats: "/dashboard/stats",
    orders: "/dashboard/orders",
    topProducts: "/dashboard/top-products",
  },
  auth: {
    login: "/auth/login",
    register: "/auth/register",
  },
  products: {
    list: "/products",
    detail: (id) => `/products/${id}`,
  },
  orders: {
    list: "/orders",
    detail: (id) => `/orders/${id}`,
    create: "/orders",
  },
};
