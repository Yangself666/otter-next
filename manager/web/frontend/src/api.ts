export interface Session {
  authenticated: boolean;
  csrfToken: string;
  user: { id?: number; name?: string; role?: string };
}
let csrfToken = "";

export async function api<T = any>(
  path: string,
  method = "GET",
  body?: unknown,
): Promise<T> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (method !== "GET") headers["X-XSRF-TOKEN"] = csrfToken;
  if (body !== undefined)
    headers["Content-Type"] =
      body instanceof URLSearchParams
        ? "application/x-www-form-urlencoded"
        : "application/json";
  const response = await fetch(`/api${path}`, {
    method,
    headers,
    credentials: "same-origin",
    body:
      body === undefined
        ? undefined
        : body instanceof URLSearchParams
          ? body.toString()
          : JSON.stringify(body),
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    if (response.status === 401 && path !== "/login")
      window.dispatchEvent(new Event("session-expired"));
    throw new Error(
      error.message ||
        error.detail ||
        (response.status === 403
          ? "当前账号没有操作权限，或页面会话已过期，请刷新后重试"
          : `请求失败（${response.status}）`),
    );
  }
  if (response.status === 204 || response.headers.get("content-length") === "0")
    return undefined as T;
  const text = await response.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}

export async function getSession() {
  const session = await api<Session>("/session");
  csrfToken = session.csrfToken;
  return session;
}
