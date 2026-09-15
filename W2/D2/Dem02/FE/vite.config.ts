import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Il WebSocket passa dal server di sviluppo e viene inoltrato al backend.
      // Senza `ws: true` la richiesta di upgrade resta a Vite, che non sa
      // parlare STOMP: nel browser si legge un errore di handshake, che non
      // nomina il proxy (slide 33 e 46).
      '/ws': { target: 'http://localhost:8080', ws: true },
    },
  },
})
