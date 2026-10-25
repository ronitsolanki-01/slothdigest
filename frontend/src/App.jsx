import { useState } from "react";
import { getSession, setSession } from "./api";
import Login from "./Login.jsx";
import Upload from "./Upload.jsx";
import Feed from "./Feed.jsx";

export default function App() {
  const [session, setSessionState] = useState(getSession());
  const [tab, setTab] = useState("digest");

  const handleAuth = (s) => {
    setSession(s);
    setSessionState(s);
  };
  const logout = () => handleAuth(null);

  if (!session) return <Login onAuth={handleAuth} />;

  const tabClass = (name) =>
    `px-4 py-2 rounded-lg text-sm font-medium transition ${
      tab === name ? "bg-moss text-white" : "text-bark/70 hover:bg-leaf"
    }`;

  return (
    <div className="min-h-screen">
      <header className="border-b border-bark/10 bg-white/60">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-3">
          <h1 className="text-lg font-semibold">🦥 SlothDigest</h1>
          <div className="flex items-center gap-3 text-sm">
            <span className="hidden text-bark/60 sm:inline">Hi, {session.displayName}</span>
            <button onClick={logout} className="rounded-lg px-3 py-1.5 text-bark/70 hover:bg-leaf">
              Log out
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-3xl px-4 py-6">
        <nav className="mb-6 flex gap-2">
          <button className={tabClass("digest")} onClick={() => setTab("digest")}>
            Digest
          </button>
          <button className={tabClass("upload")} onClick={() => setTab("upload")}>
            Upload chats
          </button>
        </nav>
        {tab === "digest" ? <Feed goUpload={() => setTab("upload")} /> : <Upload onDone={() => setTab("digest")} />}
      </main>
    </div>
  );
}
