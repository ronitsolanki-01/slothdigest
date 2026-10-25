# SlothDigest

Upload your group-chat exports, go to sleep, and read a two-line summary per chat in the morning
(plus action items and anything that mentions you). It runs on an open-weight model through Ollama,
so your chats never leave your machine.

**Backend:** Spring Boot 4.0, Spring AI 2.0 (Ollama), Spring Security 7 (JWT), JPA + H2, `@Scheduled` job
**Frontend:** React + Vite + Tailwind

## Prerequisites
- Java 17+ (21 recommended), Maven 3.9+
- Node 18+
- [Ollama](https://ollama.com) running locally, with the model pulled: `ollama pull qwen3.5:4b`
  (change the model in `application.properties`; thinking is turned off in `DigestAi.java`)

## Run
```bash
# 1. backend (http://localhost:8080)
cd backend
mvn spring-boot:run

# 2. frontend (http://localhost:5173), in another terminal
cd frontend
npm install
npm run dev
```

## Try it
1. Open http://localhost:5173 and create an account. Use the name your friends call you as
   **display name**, because that is how mentions of you are found.
2. Upload `sample-chat.txt` (WhatsApp: open a chat, More, Export chat, Without media).
3. Click **Summarize now**, or wait for the nightly run (3 AM by default, see `app.digest.cron`).
4. Open the **Digest** tab.

## API
| Method | Path | Auth | What it does |
|---|---|---|---|
| POST | `/api/auth/register` | no | create account, returns JWT |
| POST | `/api/auth/login` | no | returns JWT |
| POST | `/api/chats` | yes | upload a chat export (multipart `file`, optional `name`) |
| GET | `/api/chats` | yes | your uploads and their status |
| GET | `/api/digests` | yes | your digests, newest first |
| POST | `/api/digests/run` | yes | summarize your pending chats now |

## Config
Everything lives in `backend/src/main/resources/application.properties`.
Set a real secret before deploying: `JWT_SECRET=<32+ random characters>`.

## Notes
- Chats that fail (for example Ollama is not running) are marked FAILED and retried on the next run.
- Only the most recent `app.digest.max-chars` characters of a chat are sent to the model.
- Small models sometimes return malformed JSON. A retry usually fixes it, or try a larger model.

## Troubleshooting
- **Vite error "Cannot find module 'tailwindcss'" mentioning a postcss.config.js outside this folder:**
  an old `postcss.config.js` (or `tailwind.config.js`) in a parent folder such as Downloads is being picked up.
  `vite.config.js` already ignores it, but you can also delete that stray file.
