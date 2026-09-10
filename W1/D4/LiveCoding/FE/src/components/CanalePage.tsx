import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError, api } from '../api/client'
import type { Canale } from '../api/types'
import { useAuth } from '../auth/authContext'

interface Props {
  idCanale: string
  /** Dati gia' noti dall'elenco: evitano lo sfarfallio mentre arriva il dettaglio. */
  canaleIniziale: Canale | null
  onIndietro: () => void
  /** Segnala alla schermata principale che le iscrizioni sono cambiate. */
  onIscrizioniCambiate: () => void
}

function messaggioErrore(err: unknown, ripiego: string) {
  return err instanceof ApiError ? [err.message, ...err.dettagli].join(' — ') : ripiego
}

export function CanalePage({ idCanale, canaleIniziale, onIndietro, onIscrizioniCambiate }: Props) {
  const { sessione } = useAuth()
  const [canale, setCanale] = useState<Canale | null>(canaleIniziale)
  const [iscritto, setIscritto] = useState(false)
  const [versione, setVersione] = useState(0)
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState<string | null>(null)

  const [messaggio, setMessaggio] = useState('')
  const [esito, setEsito] = useState<string | null>(null)
  const [inCorso, setInCorso] = useState(false)

  const proprietario = canale != null && canale.idUtente === sessione?.id

  useEffect(() => {
    let annullato = false

    async function carica() {
      try {
        const [dettaglio, iscrizioni] = await Promise.all([
          api.canale(idCanale),
          api.canaliIscritto(0),
        ])
        if (annullato) return
        setCanale(dettaglio)
        setIscritto(iscrizioni.content.some((c) => c.id === idCanale))
        setErrore(null)
      } catch (err) {
        if (!annullato) setErrore(messaggioErrore(err, 'Canale non raggiungibile'))
      } finally {
        if (!annullato) setCaricamento(false)
      }
    }

    void carica()
    return () => {
      annullato = true
    }
  }, [idCanale, versione])

  async function cambiaIscrizione() {
    try {
      if (iscritto) await api.unfollow(idCanale)
      else await api.follow(idCanale)
      setVersione((v) => v + 1)
      onIscrizioniCambiate()
    } catch (err) {
      setErrore(messaggioErrore(err, 'Operazione non riuscita'))
    }
  }

  async function invia(e: FormEvent) {
    e.preventDefault()
    setErrore(null)
    setEsito(null)
    setInCorso(true)
    try {
      const risposta = await api.creaNotifica({ tipo: 'CANALE', message: messaggio, idCanale })
      setEsito(
        risposta.destinatari === 0
          ? 'Nessun iscritto al canale: la notifica non ha destinatari.'
          : `Inviata a ${risposta.destinatari} iscritti, ${risposta.inviateViaWebSocket} online in questo momento.`,
      )
      setMessaggio('')
    } catch (err) {
      setErrore(messaggioErrore(err, 'Invio non riuscito'))
    } finally {
      setInCorso(false)
    }
  }

  return (
    <section className="colonna">
      <button className="link-azione" onClick={onIndietro}>
        ← Torna ai canali
      </button>

      {errore && <p className="avviso errore">{errore}</p>}

      {caricamento && !canale ? (
        <p className="vuoto">Caricamento…</p>
      ) : !canale ? (
        <p className="vuoto">Canale non trovato.</p>
      ) : (
        <>
          <div className="card testata-canale">
            <div className="testata-canale-titolo">
              <h2>{canale.nome}</h2>
              {proprietario ? (
                <span className="etichetta tipo-personal">Tuo canale</span>
              ) : iscritto ? (
                <span className="etichetta tipo-canale">Iscritto</span>
              ) : null}
            </div>
            <p className="descrizione-canale">{canale.descrizione || <em>Nessuna descrizione</em>}</p>

            {!proprietario && (
              <button
                className={iscritto ? 'bottone piccolo attenuato' : 'bottone piccolo primario'}
                onClick={() => void cambiaIscrizione()}
              >
                {iscritto ? 'Smetti di seguire' : 'Segui il canale'}
              </button>
            )}
          </div>

          <form className="card riquadro-invio" onSubmit={invia}>
            <h3 className="titolo-sezione">Invia una notifica al canale</h3>
            <p className="nota">
              La riceveranno tutti gli iscritti a <strong>{canale.nome}</strong>. Chi è online la vede
              subito, gli altri la trovano nella loro lista.
            </p>

            <label className="campo">
              <span>Messaggio</span>
              <textarea
                value={messaggio}
                onChange={(e) => setMessaggio(e.target.value)}
                rows={3}
                placeholder="Cosa vuoi comunicare agli iscritti"
                required
              />
            </label>

            {esito && <p className="avviso successo">{esito}</p>}

            <button className="bottone primario" disabled={inCorso}>
              {inCorso ? 'Invio…' : 'Invia al canale'}
            </button>
          </form>
        </>
      )}
    </section>
  )
}
