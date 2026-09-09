import { useState } from 'react'

const API = 'http://localhost:8080/api'

type ExtractedText = { text: string; millis: number }
type ApiError = { error: string; detail: string }

export default function App() {
  const [file, setFile] = useState<File | null>(null)
  const [anteprima, setAnteprima] = useState<string | null>(null)
  const [lingua, setLingua] = useState('it')
  const [risultato, setRisultato] = useState<ExtractedText | null>(null)
  const [errore, setErrore] = useState<ApiError | null>(null)
  const [inCorso, setInCorso] = useState(false)

  function scegli(scelto: File | null) {
    setFile(scelto)
    setRisultato(null)
    setErrore(null)
    // L'URL dell'anteprima precedente va rilasciato, altrimenti resta in memoria.
    setAnteprima((precedente) => {
      if (precedente) URL.revokeObjectURL(precedente)
      return scelto ? URL.createObjectURL(scelto) : null
    })
  }

  async function estrai() {
    if (!file) return

    setInCorso(true)
    setRisultato(null)
    setErrore(null)

    // Un file solo: il corpo e' l'immagine cosi' com'e', senza multipart.
    const res = await fetch(`${API}/extract?lang=${lingua}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/octet-stream' },
      body: file,
    })

    if (res.ok) setRisultato(await res.json())
    else setErrore(await res.json())
    setInCorso(false)
  }

  return (
    <main>
      <h1>OCR di un'immagine</h1>
      <p className="sub">Le immagini di prova sono in BE/samples: nitida.png e sfocata.png.</p>

      <section className="card">
        <div className="row">
          <input type="file" accept="image/*" onChange={(e) => scegli(e.target.files?.[0] ?? null)} />
          <select value={lingua} onChange={(e) => setLingua(e.target.value)}>
            <option value="it">italiano (ita)</option>
            <option value="en">inglese (eng)</option>
          </select>
          <button onClick={estrai} disabled={inCorso || !file}>
            {inCorso ? 'Estrazione in corso...' : 'Estrai testo'}
          </button>
          {file && <small>{(file.size / 1024).toFixed(0)} KB</small>}
        </div>
        {anteprima && <img src={anteprima} alt="anteprima" style={{ maxWidth: '100%' }} />}
      </section>

      {risultato && (
        <section className="card">
          <h2>Testo estratto ({risultato.millis} ms)</h2>
          {/* L'OCR restituisce un'ipotesi: il testo va mostrato in un campo modificabile. */}
          <textarea key={risultato.millis} defaultValue={risultato.text} rows={10} />
        </section>
      )}

      {errore && (
        <section className="card errore">
          <h2>Errore</h2>
          <p>
            {errore.error} — {errore.detail}
          </p>
        </section>
      )}
    </main>
  )
}
