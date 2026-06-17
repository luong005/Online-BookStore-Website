import { apiFetch } from "../../shared/api/httpClient";

export const orderService = {
  createPayment(payload, token) {
    return apiFetch("/api/order/payment", {
      method: "POST",
      token,
      body: payload,
    });
  },

  getPaymentStatus(orderCode, token) {
    return apiFetch(`/api/order/payment-status/${orderCode}`, { token });
  },

  getOrders(token) {
    return apiFetch("/api/orders", { token });
  },

  getOrderDetail(orderId, token) {
    return apiFetch(`/api/order-${orderId}`, { token });
  },

};
