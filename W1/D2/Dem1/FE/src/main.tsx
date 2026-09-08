import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './index.css'

// Il punto di partenza del frontend: prende il <div id="root"> di index.html
// e ci disegna dentro il componente App.
//
// StrictMode e' un aiuto solo in sviluppo: React monta i componenti due volte
// per far emergere subito gli effetti scritti male. In produzione sparisce.
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
