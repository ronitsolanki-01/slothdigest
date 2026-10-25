import { useEffect, useState } from "react";
import { api } from "./api";

const badge = {
  PENDING: "bg-amber-100 text-amber-800",
  DONE: "bg-leaf text-moss-dark",
  FAILED: "bg-red-100 text-red-700",
};

export default function Upload({ onDone }) {
  const [chats, setChats] = useState([]);
  const [file, setFile] = useState(null);
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [uploading, setUploading] = useState(false);
  const [running, setRunning] = useState(false);

  const load = () => api.listChats().then(setChats).catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, []);

  const upload = async (e) => {
    e.preventDefault();
    if (!file) return;
    setError("");
    setUploading(true);
    try {
      await api.uploadChat(file, name);
      setFile(null);
      setName("");
      e.target.reset();
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setUploading(false);
    }
  };

  const runNow = async () => {
    setError("");
    setRunning(true);
    try {
      await api.runDigest();
      await load();
      onDone();
    } catch (err) {
      setError(err.message);
    } finally {
      setRunning(false);
    }
  };

  const pending = chats.filter((c) => c.status !== "DONE").length;

  return (
    <div className="space-y-6">
      <form onSubmit={upload} className="space-y-3 rounded-2xl bg-white p-5 shadow-sm">
        <h2 className="font-semibold">Add a chat export</h2>
        <p className="text-sm text-bark/60">
          In WhatsApp, open a chat, choose Export chat, then Without media. Upload the .txt file here.
        </p>
        <input
          type="file"
          accept=".txt,text/plain"
          onChange={(e) => setFile(e.target.files[0] || null)}
          className="block w-full text-sm file:mr-3 file:rounded-lg file:border-0 file:bg-leaf file:px-3 file:py-2 file:text-moss-dark"
        />
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Chat name (optional)"
          className="w-full rounded-lg border border-bark/20 px-3 py-2 text-sm outline-none focus:border-moss"
        />
        <button
          disabled={!file || uploading}
          className="rounded-lg bg-moss px-4 py-2 text-sm font-medium text-white hover:bg-moss-dark disabled:opacity-50"
        >
          {uploading ? "Uploading..." : "Upload chat"}
        </button>
      </form>

      {error && <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

      <section className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="font-semibold">Your chats</h2>
          <button
            onClick={runNow}
            disabled={running || pending === 0}
            className="rounded-lg border border-moss px-3 py-1.5 text-sm text-moss hover:bg-leaf disabled:opacity-40"
          >
            {running ? "Summarizing, this can take a minute..." : `Summarize now (${pending})`}
          </button>
        </div>
        <p className="text-xs text-bark/50">Not in a hurry? Pending chats are summarized automatically overnight.</p>

        {chats.length === 0 ? (
          <p className="rounded-2xl bg-white p-5 text-sm text-bark/60 shadow-sm">No chats uploaded yet.</p>
        ) : (
          <ul className="divide-y divide-bark/10 rounded-2xl bg-white shadow-sm">
            {chats.map((c) => (
              <li key={c.id} className="flex items-start justify-between gap-3 px-5 py-3">
                <div>
                  <p className="font-medium">{c.chatName}</p>
                  <p className="text-xs text-bark/50">{new Date(c.uploadedAt).toLocaleString()}</p>
                  {c.error && <p className="mt-1 text-xs text-red-700">{c.error}</p>}
                </div>
                <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${badge[c.status]}`}>{c.status.toLowerCase()}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
