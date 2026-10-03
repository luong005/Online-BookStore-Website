import { apiFetch } from "../../shared/api/httpClient";

function extractUsers(data) {
  return Array.isArray(data)
    ? data
    : data?.userInfoResponseDTOS || data?.userInfoResponseDTOList || data?.userDTOList || data?.users || [];
}

function getUserKey(user, index) {
  return user?.id || user?.userId || user?.phone_number || user?.phoneNumber || `user-${index}`;
}

function mergeUserResponses(responses) {
  const userMap = new Map();

  responses.forEach((data) => {
    extractUsers(data).forEach((user, index) => {
      userMap.set(getUserKey(user, index), user);
    });
  });

  const userDTOList = [...userMap.values()];
  return {
    userDTOList,
    numberOfUsers: userDTOList.length,
  };
}

function requestUsersByRole(filters, roleId, token, options = {}) {
  const body = {
    address: filters.address || "",
  };

  if (roleId !== undefined && roleId !== null && String(roleId).trim() !== "") {
    body.role_id = roleId;
    body.roleId = roleId;
  }

  return apiFetch("/__bookshop/admin-users", {
    method: "POST",
    token,
    local: true,
    body,
    ...options,
  });
}

export const adminService = {
  getRevenue(month, token, options = {}) {
    return apiFetch(`/api/admin/dashboard/revenue-${month}`, { token, ...options });
  },

  getBestSellers(top, token, options = {}) {
    return apiFetch(`/api/admin/dashboard/best-selling-books-${top}`, { token, ...options });
  },

  async getUsers(filters = {}, token, options = {}) {
    const roleId = String(filters.role_id || "").trim();

    if (roleId) {
      return requestUsersByRole(filters, roleId, token, options);
    }

    return requestUsersByRole(filters, "", token, options);
  },
};
