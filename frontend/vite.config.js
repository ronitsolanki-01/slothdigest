import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  // Tailwind v4 runs through its Vite plugin, so no PostCSS plugins are needed.
  // An inline (even empty) config stops Vite from picking up a stray postcss.config.js
  // from a parent folder such as Downloads.
  css: { postcss: { plugins: [] } },
  server: {
    port: 5173,
    // forward API calls to Spring Boot, so no CORS setup is needed in dev
    proxy: { "/api": "http://localhost:8080" },
  },
});
