export function decodeJwtPayload(token) {
  if (!token || !token.includes(".")) return null;

  try {
    const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join(""),
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function isAdminAccount(token, profile) {
  const payload = decodeJwtPayload(token) || {};
  const collectRoleText = (value) => {
    if (!value) return [];
    if (Array.isArray(value)) return value.flatMap(collectRoleText);
    if (typeof value === "object") return Object.values(value).flatMap(collectRoleText);
    return [String(value).toUpperCase()];
  };

  const roleValues = [
    payload.role,
    payload.roles,
    payload.authorities,
    payload.authority,
    payload.scope,
    payload.scopes,
    profile?.role,
    profile?.roles,
    profile?.authorities,
    profile?.roleName,
    profile?.role_name,
  ]
    .flat()
    .filter(Boolean)
    .flatMap(collectRoleText);

  return (
    roleValues.some((value) => value.includes("ADMIN")) ||
    Number(profile?.role_id || profile?.roleId) === 1
  );
}
