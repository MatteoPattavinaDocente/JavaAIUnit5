import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { APIProvider } from '@vis.gl/react-google-maps'
import App from './App'
import './index.css'

// APIProvider carica lo script di Google: una volta sola, alla radice.
// Dentro il componente mappa verrebbe rimontato a ogni smontaggio.
// In JavaScript non serve il "!" dopo getElementById: senza TypeScript nessuno
// ci chiede di garantire che l'elemento esista. Se il div #root manca in
// index.html, l'errore arriva a runtime (createRoot su null) invece che in fase
// di compilazione: e' il prezzo di non avere i tipi.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    {/* Vite espone solo le variabili col prefisso VITE_. La chiave finisce
        comunque nel bundle: si protegge con le restrizioni in Cloud Console. */}
    <APIProvider apiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY}>
      <App />
    </APIProvider>
  </StrictMode>,
)
