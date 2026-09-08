import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  // sockjs-client presuppone l'ambiente Node e cerca la variabile global, che nel
  // browser non esiste e che Vite non definisce. Senza questa riga: "global is not defined".
  define: { global: 'globalThis' },
})
