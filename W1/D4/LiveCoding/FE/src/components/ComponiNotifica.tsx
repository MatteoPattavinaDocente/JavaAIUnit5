import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError, api } from '../api/client'

/** Solo i tipi non legati a un canale: quelle di canale si inviano dalla pagina del canale. */
type TipoDiSistema = 'ALL' | 'PERSONAL'

export function ComponiNotifica() {
  const [tipo, setTipo] = useState<TipoDiSistema>('ALL')
  const [messaggio, setMessaggio] = useState('')
  const [idDestinatario, setIdDestinatario] = useState('')
  const [esito, setEsito] = useState<string | null>(null)
  const [errore, setErrore] = useState<string | null>(null)
  const [inCorso, setInCorso] = useState(false)

  async function invia(e: FormEvent) {
    e.preventDefault()
    setErrore(null)
    setEsito(null)
    setInCorso(true)
    try {
      const risposta = await api.creaNotifica({
        tipo,
        message: messaggio,
        idDestinatario: tipo === 'PERSONAL' ? idDestinatario : null,
      })
      setEsito(
        `Create ${risposta.destinatari} notifiche, ${risposta.inviateViaWebSocket} recapitate subito via WebSocket.`,
      )
      setMessaggio('')
    } catch (err) {
      setErrore(err instanceof ApiError ? [err.message, ...err.dettagli].join(' — ') : 'Invio non riuscito')
    } finally {
      setInCorso(false)
    }
  }

  return (
    <form className="card riquadro-invio" onSubmit={invia}>
      <h2 className="titolo-sezione">Notifica di sistema</h2>

      <div className="tab-gruppo">
        <button type="button" className={tipo === 'ALL' ? 'tab attiva' : 'tab'} onClick={() => setTipo('ALL')}>
          A tutti
        </button>
        <button
          type="button"
          className={tipo === 'PERSONAL' ? 'tab attiva' : 'tab'}
          onClick={() => setTipo('PERSONAL')}
        >
          A un utente
        </button>
      </div>

      {tipo === 'ALL' ? (
        <p className="nota">Destinatari: tutti gli utenti registrati.</p>
      ) : (
        <label className="campo">
          <span>ID utente destinatario</span>
          <input
            value={idDestinatario}
            onChange={(e) => setIdDestinatario(e.target.value)}
            placeholder="UUID dell'utente"
            required
          />
        </label>
      )}

      <label className="campo">
        <span>Messaggio</span>
        <textarea
          value={messaggio}
          onChange={(e) => setMessaggio(e.target.value)}
          rows={3}
          placeholder="Cosa vuoi comunicare"
          required
        />
      </label>

      {errore && <p className="avviso errore">{errore}</p>}
      {esito && <p className="avviso successo">{esito}</p>}

      <button className="bottone primario" disabled={inCorso}>
        {inCorso ? 'Invio…' : 'Invia'}
      </button>
    </form>
  )
}
