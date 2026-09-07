import { useState } from 'react'

import { ApiError, geocodeAddress } from '../api'
import type { GeocodeResult } from '../types'

interface Props {
  onFound: (result: GeocodeResult) => void
}

/** Il geocoding lo fa il backend: qui si manda solo il testo digitato. */
export function AddressSearch({ onFound }: Props) {
  const [address, setAddress] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [found, setFound] = useState<string | null>(null)

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    if (!address.trim()) {
      return
    }
    setBusy(true)
    setError(null)
    setFound(null)
    try {
      const result = await geocodeAddress(address)
      setFound(result.formattedAddress)
      onFound(result)
    }
    catch (cause) {
      setError(describe(cause))
    }
    finally {
      setBusy(false)
    }
  }

  return (
    <form className="card" onSubmit={submit}>
      <h2>Cerca un indirizzo</h2>
      <div className="row">
        <input
          value={address}
          onChange={(event) => setAddress(event.target.value)}
          placeholder="Via Roma 1, Milano"
        />
        <button type="submit" disabled={busy}>{busy ? '...' : 'Vai'}</button>
      </div>
      {found && <p className="hint">{found}</p>}
      {error && <p className="error">{error}</p>}
    </form>
  )
}

/** Ogni status del backend ha un messaggio dedicato. */
function describe(cause: unknown): string {
  if (!(cause instanceof ApiError)) {
    return 'Ricerca non riuscita'
  }
  switch (cause.status) {
    case 400:
      return 'Indirizzo non valido'
    case 404:
      return 'Nessun risultato per questo indirizzo'
    case 429:
      return 'Troppe richieste al servizio di geocoding: riprova fra poco'
    case 502:
      return `Il servizio di geocoding ha rifiutato la richiesta (${cause.message})`
    case 503:
      return 'Servizio di geocoding non raggiungibile'
    default:
      return cause.message
  }
}
