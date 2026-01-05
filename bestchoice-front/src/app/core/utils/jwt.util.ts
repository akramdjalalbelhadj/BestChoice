export function decodeJwtPayload(token: string): any | null {
  try {
    const part = token.split('.')[1];
    const json = atob(part.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decodeURIComponent(escape(json)));
  } catch {
    return null;
  }
}
export function isJwtExpired(token: string): boolean {
  const payload = decodeJwtPayload(token);
  if (payload && payload.exp) {
    const now = Math.floor(Date.now() / 1000);
    return now >= payload.exp;
  }
  return true;
}
