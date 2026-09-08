import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './index.css'

// StrictMode, in sviluppo, monta ogni componente due volte di proposito.
// Qui serve a verificare che la pulizia dell'useEffect chiuda davvero la prima
// connessione: se non lo facesse, vedremmo ogni notifica arrivare in doppio.
//
// In TSX c'era un punto esclamativo dopo getElementById('root'): prometteva al
// compilatore che quel div esiste. Senza TypeScript non c'e' nessuno da rassicurare.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
