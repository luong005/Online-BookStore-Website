import { apiFetch } from "../../shared/api/httpClient";

export const cartService = {
  getCart(token) {
    return apiFetch("/api/cart", { token });
  },

  getCheckout(token) {
    return apiFetch("/api/cart/checkout", { token });
  },

  addItem(payload, token) {
    return apiFetch("/api/cart/items", {
      method: "POST",
      token,
      body: payload,
    });
  },

  updateItem(payload, token) {
    return apiFetch("/api/cart/item", {
      method: "PUT",
      token,
      body: payload,
    });
  },

  removeItem(cartItemId, token) {
    return apiFetch(`/api/cart/item-${cartItemId}`, {
      method: "DELETE",
      token,
    });
  },
};
