import { useState } from 'react'
import Camera from './Camera'

const API = 'http://localhost:8080/api'
const DOCUMENT_ID = 1

// ScanResult = { storageKey: string, sizeBytes: number, text: string, millis: number }

export default function App() {
  const [scatto, setScatto] = useState(null)
  const [risultato, setRisultato] = useState(null)
  const [testo, setTesto] = useState('')
  const [errore, setErrore] = useState(null)
  const [inCorso, setInCorso] = useState(false)

  async function invia(file) {
    setInCorso(true)
    setErrore(null)
    setRisultato(null)

    const form = new FormData()
    form.append('file', file)

    const res = await fetch(`${API}/documents/${DOCUMENT_ID}/scan`, { method: 'POST', body: form })

    if (res.ok) {
      const esito = await res.json()
      setRisultato(esito)
      setTesto(esito.text)
    } else {
      setErrore(`HTTP ${res.status} — ${await res.text()}`)
    }
    setInCorso(false)
  }

  return (
    <main>
      <h1>Scansione documento {DOCUMENT_ID}</h1>

      <Camera
        onCapture={(file) => {
          setScatto(file)
          invia(file)
        }}
      />

      {scatto && (
        <p>
          Scatto: {(scatto.size / 1024).toFixed(0)} KB — {scatto.type}
        </p>
      )}

      {inCorso && <p>Estrazione in corso...</p>}
      {errore && <p className="errore">{errore}</p>}

      {risultato && (
        <section className="card">
          <h2>Testo estratto ({risultato.millis} ms)</h2>
          {/* L'OCR restituisce un'ipotesi: l'utente deve poterla correggere. */}
          <textarea value={testo} onChange={(e) => setTesto(e.target.value)} rows={10} />
          <small>
            salvato come {risultato.storageKey} — {risultato.sizeBytes} byte
          </small>
        </section>
      )}
    </main>
  )
}
