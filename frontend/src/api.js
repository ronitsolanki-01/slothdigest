const SESSION_KEY = "slothdigest_session";

export const getSession = () => {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY));
  } catch {
    return null;
  }
};

export const setSession = (session) => {
  if (session) localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  else localStorage.removeItem(SESSION_KEY);
};

async function request(path, { json, body, method = "GET" } = {}) {
  const session = getSession();
  const headers = {};
  if (session?.token) headers.Authorization = `Bearer ${session.token}`;
  if (json) headers["Content-Type"] = "application/json";

  const res = await fetch(`/api${path}`, {
    method,
    headers,
    body: json ? JSON.stringify(json) : body,
  });

  if (res.status === 401 && session) {
    // token expired or invalid: send the person back to the login screen
    setSession(null);
    window.location.reload();
  }
  if (!res.ok) {
    let message = `Something went wrong (${res.status})`;
    try {
      const data = await res.json();
      if (data.message) message = data.message;
    } catch {
      /* response had no JSON body */
    }
    throw new Error(message);
  }
  return res.json();
}

export const api = {
  register: (username, password, displayName) =>
    request("/auth/register", { method: "POST", json: { username, password, displayName } }),
  login: (username, password) =>
    request("/auth/login", { method: "POST", json: { username, password } }),
  listChats: () => request("/chats"),
  uploadChat: (file, name) => {
    const form = new FormData();
    form.append("file", file);
    if (name) form.append("name", name);
    return request("/chats", { method: "POST", body: form });
  },
  listDigests: () => request("/digests"),
  runDigest: () => request("/digests/run", { method: "POST" }),
};
