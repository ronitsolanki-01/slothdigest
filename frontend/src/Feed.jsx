import { useEffect, useState } from "react";
import { api } from "./api";

export default function Feed({ goUpload }) {
  const [digests, setDigests] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.listDigests().then(setDigests).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>;
  if (!digests) return <p className="text-sm text-bark/60">Loading...</p>;

  if (digests.length === 0) {
    return (
      <div className="rounded-2xl bg-white p-8 text-center shadow-sm">
        <p className="text-lg font-medium">No digests yet</p>
        <p className="mt-1 text-sm text-bark/60">Upload a chat export and your first summary will show up here.</p>
        <button onClick={goUpload} className="mt-4 rounded-lg bg-moss px-4 py-2 text-sm font-medium text-white hover:bg-moss-dark">
          Upload a chat
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {digests.map((d) => (
        <article key={d.id} className="space-y-4 rounded-2xl bg-white p-5 shadow-sm">
          <header className="flex items-baseline justify-between gap-3">
            <h2 className="text-lg font-semibold">{d.chatName}</h2>
            <span className="shrink-0 text-xs text-bark/50">
              {d.messageCount} messages, {new Date(d.createdAt).toLocaleDateString()}
            </span>
          </header>

          <p className="leading-relaxed">{d.summary}</p>

          {d.mentions.length > 0 && (
            <div className="rounded-xl bg-amber-50 p-3">
              <h3 className="mb-1 text-sm font-semibold text-amber-900">Mentions you</h3>
              <ul className="list-disc space-y-1 pl-5 text-sm text-amber-900">
                {d.mentions.map((m, i) => (
                  <li key={i}>{m}</li>
                ))}
              </ul>
            </div>
          )}

          {d.actionItems.length > 0 && (
            <div>
              <h3 className="mb-1 text-sm font-semibold">To do</h3>
              <ul className="space-y-1 text-sm">
                {d.actionItems.map((a, i) => (
                  <li key={i} className="flex gap-2">
                    <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-moss" />
                    {a}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </article>
      ))}
    </div>
  );
}
