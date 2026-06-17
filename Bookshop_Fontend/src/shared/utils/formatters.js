export function formatMoney(value) {
  const number = Number(value || 0);

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(number);
}

export function formatDateTime(value) {
  if (!value) return "Chưa có ngày";

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export function normalizeList(value) {
  return Array.isArray(value) ? value : [];
}

export function getErrorMessage(error) {
  return error?.message || "Co loi xay ra. Vui long thu lai.";
}
