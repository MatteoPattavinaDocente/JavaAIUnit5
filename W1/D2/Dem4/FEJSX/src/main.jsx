import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './index.css'

// StrictMode, in sviluppo, monta ogni componente due volte di proposito.
// Qui serve a verificare che il client STOMP venga davvero disattivato dalla
// pulizia dell'useEffect: se non lo fosse, resterebbero due sessioni aperte
// e ogni notifica arriverebbe in doppio.
//
// In TSX c'era un punto esclamativo dopo getElementById('root'), per promettere al
// compilatore che quel div esiste. Senza TypeScript non serve dirlo a nessuno.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
