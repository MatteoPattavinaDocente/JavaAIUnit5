import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

// Nella versione TypeScript c'era getElementById('root')! : il "!" prometteva al
// compilatore che l'elemento esiste. In JavaScript la promessa non serve, ma se il
// div #root manca in index.html l'errore arriva solo a runtime.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
