import { useState } from 'react'

import { ApiError, createReport } from '../api'
import { CATEGORIES, CATEGORY_LABEL, type Category, type Report } from '../types'

interface Props {
  position: google.maps.LatLngLiteral
  onCreated: (report: Report) => void
  onCancel: () => void
}

/** Le coordinate arrivano gia' compilate dal click e non sono modificabili a mano. */
export function ReportForm({ position, onCreated, onCancel }: Props) {
  const [category, setCategory] = useState<Category>('BUCA')
  const [description, setDescription] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const created = await createReport({
        category,
        description,
        latitude: Number(position.lat.toFixed(6)),
        longitude: Number(position.lng.toFixed(6)),
      })
      onCreated(created)
    }
    catch (cause) {
      const message = cause instanceof ApiError
        ? [cause.message, ...cause.details].join(' - ')
        : 'Salvataggio non riuscito'
      setError(message)
    }
    finally {
      setSaving(false)
    }
  }

  return (
    <form className="card form" onSubmit={submit}>
      <h2>Nuova segnalazione</h2>

      <label>
        Categoria
        <select value={category} onChange={(event) => setCategory(event.target.value as Category)}>
          {CATEGORIES.map((value) => (
            <option key={value} value={value}>{CATEGORY_LABEL[value]}</option>
          ))}
        </select>
      </label>

      <label>
        Descrizione
        <textarea
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          rows={3}
          maxLength={500}
          required
          placeholder="Cosa hai visto?"
        />
      </label>

      <div className="coords">
        <label>
          Latitudine
          <input value={position.lat.toFixed(6)} readOnly />
        </label>
        <label>
          Longitudine
          <input value={position.lng.toFixed(6)} readOnly />
        </label>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="actions">
        <button type="submit" disabled={saving}>{saving ? 'Salvataggio...' : 'Salva'}</button>
        <button type="button" className="ghost" onClick={onCancel} disabled={saving}>Annulla</button>
      </div>
    </form>
  )
}
