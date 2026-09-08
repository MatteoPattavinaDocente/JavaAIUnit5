import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './index.css'

// Il punto di partenza del frontend: prende il <div id="root"> di index.html
// e ci disegna dentro il componente App.
//
// StrictMode e' un aiuto solo in sviluppo: React monta i componenti due volte
// per far emergere subito gli effetti scritti male. In produzione sparisce.
//
// In TSX la riga era createRoot(document.getElementById('root')!): quel punto
// esclamativo era una promessa fatta al compilatore, "fidati, l'elemento c'e'".
// Qui non serve scriverlo, e il comportamento non cambia: se quel div mancasse,
// la pagina si romperebbe in entrambe le versioni.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
