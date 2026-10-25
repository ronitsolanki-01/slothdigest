import { useState } from "react";
import { api } from "./api";

export default function Login({ onAuth }) {
  const [mode, setMode] = useState("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      const res =
        mode === "login"
          ? await api.login(username, password)
          : await api.register(username, password, displayName);
      onAuth(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const input =
    "w-full rounded-lg border border-bark/20 bg-white px-3 py-2 outline-none focus:border-moss focus:ring-2 focus:ring-moss/30";

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <form onSubmit={submit} className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-6 shadow-sm">
        <div>
          <h1 className="text-2xl font-semibold">🦥 SlothDigest</h1>
          <p className="mt-1 text-sm text-bark/60">Your group chats, summarized while you sleep.</p>
        </div>

        <label className="block text-sm">
          Username
          <input className={input} value={username} onChange={(e) => setUsername(e.target.value)} required minLength={3} />
        </label>

        {mode === "register" && (
          <label className="block text-sm">
            Display name
            <input className={input} value={displayName} onChange={(e) => setDisplayName(e.target.value)} required />
            <span className="mt-1 block text-xs text-bark/50">
              Use the name your friends call you. We use it to find messages that mention you.
            </span>
          </label>
        )}

        <label className="block text-sm">
          Password
          <input type="password" className={input} value={password} onChange={(e) => setPassword(e.target.value)} required minLength={6} />
        </label>

        {error && <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

        <button disabled={busy} className="w-full rounded-lg bg-moss py-2 font-medium text-white hover:bg-moss-dark disabled:opacity-60">
          {busy ? "Please wait..." : mode === "login" ? "Log in" : "Create account"}
        </button>

        <button
          type="button"
          onClick={() => {
            setMode(mode === "login" ? "register" : "login");
            setError("");
          }}
          className="w-full text-sm text-moss hover:underline"
        >
          {mode === "login" ? "New here? Create an account" : "Already have an account? Log in"}
        </button>
      </form>
    </div>
  );
}
