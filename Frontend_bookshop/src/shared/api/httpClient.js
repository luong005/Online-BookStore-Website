const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
let refreshPromise = null;

export class ApiError extends Error {
  constructor(message, status, payload) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

export function unwrapApiResponse(payload) {
  if (payload && typeof payload === "object" && "error" in payload) {
    if (payload.error !== 0) {
      throw new ApiError(payload.message || "Yêu cầu API thất bại", 200, payload);
    }
    return payload.data;
  }

  return payload;
}

async function parseResponsePayload(response) {
  const text = await response.text();
  let payload = text;

  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = text;
    }
  }

  return payload;
}

async function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = fetch(`${API_BASE_URL}/api/refresh`, {
      method: "POST",
      credentials: "include",
    })
      .then(async (response) => {
        const payload = await parseResponsePayload(response);
        if (!response.ok) {
          throw new ApiError("Phiên đăng nhập đã hết hạn.", response.status, payload);
        }

        const data = unwrapApiResponse(payload);
        const nextToken = data?.accessToken;
        if (!nextToken) {
          throw new ApiError("Server không trả accessToken mới.", response.status, payload);
        }

        localStorage.setItem("bookshop_token", nextToken);
        window.dispatchEvent(new CustomEvent("bookshop:token-refreshed", { detail: nextToken }));
        return nextToken;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

function getErrorMessageFromPayload(payload, status) {
  return (
    payload?.message ||
    payload?.error ||
    payload?.detail ||
    (typeof payload === "string" && payload) ||
    `Yêu cầu thất bại với mã ${status}`
  );
}

export async function apiFetch(path, options = {}) {
  const { method = "GET", body, token, headers = {}, skipRefresh = false, local = false, responseType = "json" } = options;
  const finalHeaders = { ...headers };
  const isFormData = body instanceof FormData;

  if (body !== undefined && !isFormData) {
    finalHeaders["Content-Type"] = "application/json";
  }

  if (token) {
    finalHeaders.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${local ? "" : API_BASE_URL}${path}`, {
    method,
    headers: finalHeaders,
    body: body === undefined || isFormData ? body : JSON.stringify(body),
    credentials: "include",
  });

  const payload = responseType === "blob" && response.ok ? await response.blob() : await parseResponsePayload(response);

  if (!response.ok && token && !skipRefresh && (response.status === 401 || response.status === 403)) {
    try {
      const nextToken = await refreshAccessToken();
      return apiFetch(path, { ...options, token: nextToken, skipRefresh: true });
    } catch (refreshError) {
      localStorage.removeItem("bookshop_token");
      window.dispatchEvent(new CustomEvent("bookshop:session-expired"));
      throw refreshError;
    }
  }

  if (!response.ok) {
    throw new ApiError(getErrorMessageFromPayload(payload, response.status), response.status, payload);
  }

  return responseType === "blob" ? payload : unwrapApiResponse(payload);
}

export function toQuery(params) {
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      query.set(key, value);
    }
  });

  const queryString = query.toString();
  return queryString ? `?${queryString}` : "";
}
