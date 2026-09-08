import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './index.css'

// StrictMode, in sviluppo, monta ogni componente due volte di proposito.
// Qui e' particolarmente utile: se la pulizia dell'useEffect dentro useCanale
// fosse scritta male, vedremmo ogni messaggio comparire in pagina due volte.
// In produzione StrictMode non fa niente.
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
