import { apiFetch, toQuery } from "../../shared/api/httpClient";

export const adminService = {
  getRevenue(month, token, options = {}) {
    return apiFetch(`/api/admin/dashboard/revenue-${month}`, { token, ...options });
  },

  getBestSellers(top, token, options = {}) {
    return apiFetch(`/api/admin/dashboard/best-selling-books-${top}`, { token, ...options });
  },

  getUsers(filters = {}, token, options = {}) {
    const query = toQuery({
      address: String(filters.address ?? "").trim(),
      role_id: String(filters.role_id ?? "").trim(),
    });

    return apiFetch(`/api/admin/dashboard/user${query}`, {
      method: "GET",
      token,
      ...options,
    });
  },
};
