import { apiFetch } from "../../shared/api/httpClient";

export const accountService = {
  register(payload) {
    return apiFetch("/api/register", {
      method: "POST",
      body: payload,
    });
  },

  login(payload) {
    return apiFetch("/api/login", {
      method: "POST",
      body: payload,
    });
  },

  logout(token) {
    return apiFetch("/api/logout", {
      method: "POST",
      token,
    });
  },

  getProfile(token) {
    return apiFetch("/api/profile", { token });
  },

  updateInfo(payload, token) {
    return apiFetch("/api/update-info", {
      method: "PATCH",
      token,
      body: payload,
    });
  },

  updatePassword(payload, token) {
    return apiFetch("/api/update-password", {
      method: "POST",
      token,
      body: payload,
    });
  },
};
