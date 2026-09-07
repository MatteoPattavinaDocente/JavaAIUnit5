import { useState } from 'react'

import { ApiError, createReport } from '../api'
import { CATEGORIES, CATEGORY_LABEL } from '../constants'

// Prop attese: position ({ lat, lng }), onCreated (report) => void, onCancel () => void.

/** Le coordinate arrivano gia' compilate dal click e non sono modificabili a mano. */
export function ReportForm({ position, onCreated, onCancel }) {
  const [category, setCategory] = useState('BUCA')
  const [description, setDescription] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  async function submit(event) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const created = await createReport({
        category,
        description,
        // toFixed(6) restituisce una stringa: Number() la riporta a numero, come
        // vuole il JSON del backend (BigDecimal, non testo).
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
        {/* Nella versione TSX il valore della select andava riportato al tipo
            Category con "as Category"; qui e' una stringa qualunque. */}
        <select value={category} onChange={(event) => setCategory(event.target.value)}>
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
