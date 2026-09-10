import { useEffect, useState } from 'react'
import { ApiError, api } from '../api/client'
import type { Notifica } from '../api/types'
import { useStomp } from '../ws/stompContext'

const ETICHETTA_TIPO: Record<Notifica['tipo'], string> = {
  PERSONAL: 'Personale',
  CANALE: 'Canale',
  ALL: 'Sistema',
}

function quandoRelativo(iso: string) {
  const secondi = Math.floor((Date.now() - new Date(iso).getTime()) / 1000)
  if (secondi < 60) return 'adesso'
  if (secondi < 3600) return `${Math.floor(secondi / 60)} min fa`
  if (secondi < 86400) return `${Math.floor(secondi / 3600)} h fa`
  return new Date(iso).toLocaleDateString('it-IT')
}

interface Props {
  nonLette: number
  /** Chiede alla schermata principale di rileggere il contatore. */
  onConteggio: () => void
}

export function PannelloNotifiche({ nonLette, onConteggio }: Props) {
  const { onNotifica } = useStomp()
  const [notifiche, setNotifiche] = useState<Notifica[]>([])
  const [versione, setVersione] = useState(0)
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState<string | null>(null)
  const [lampeggia, setLampeggia] = useState(false)

  useEffect(() => {
    let annullato = false

    async function carica() {
      try {
        const pagina = await api.notifiche(0)
        if (annullato) return
        setNotifiche(pagina.content)
        setErrore(null)
      } catch (err) {
        if (!annullato) setErrore(err instanceof ApiError ? err.message : 'Backend non raggiungibile')
      } finally {
        if (!annullato) setCaricamento(false)
      }
    }

    void carica()
    return () => {
      annullato = true
    }
  }, [versione])

  useEffect(() => {
    // arrivo da WebSocket: la notifica entra in testa senza ricaricare la lista
    return onNotifica((notifica) => {
      setNotifiche((precedenti) =>
        precedenti.some((n) => n.id === notifica.id) ? precedenti : [notifica, ...precedenti],
      )
      onConteggio()
      setLampeggia(true)
      setTimeout(() => setLampeggia(false), 1200)
    })
  }, [onNotifica, onConteggio])

  async function marcaLetta(id: string) {
    try {
      const aggiornata = await api.marcaLetta(id)
      setNotifiche((precedenti) => precedenti.map((n) => (n.id === id ? aggiornata : n)))
      onConteggio()
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : 'Operazione non riuscita')
    }
  }

  async function marcaTutte() {
    try {
      await api.marcaTutteLette()
      setVersione((v) => v + 1)
      onConteggio()
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : 'Operazione non riuscita')
    }
  }

  return (
    <section className={lampeggia ? 'colonna pannello-notifiche evidenzia' : 'colonna pannello-notifiche'}>
      <div className="intestazione-sezione">
        <h2 className="titolo-sezione">
          Notifiche
          {nonLette > 0 && <span className="badge">{nonLette}</span>}
        </h2>
        <button className="bottone piccolo" onClick={() => void marcaTutte()} disabled={nonLette === 0}>
          Segna tutte lette
        </button>
      </div>

      {errore && <p className="avviso errore">{errore}</p>}

      {caricamento ? (
        <p className="vuoto">Caricamento…</p>
      ) : notifiche.length === 0 ? (
        <p className="vuoto">Nessuna notifica.</p>
      ) : (
        <ul className="lista-notifiche">
          {notifiche.map((n) => (
            <li key={n.id} className={n.readAt ? 'card notifica letta' : 'card notifica'}>
              <div className="notifica-testa">
                <span className={`etichetta tipo-${n.tipo.toLowerCase()}`}>{ETICHETTA_TIPO[n.tipo]}</span>
                <span className="quando">{quandoRelativo(n.createdAt)}</span>
              </div>
              <p className="notifica-messaggio">{n.message}</p>
              {!n.readAt && (
                <button className="link-azione" onClick={() => void marcaLetta(n.id)}>
                  Segna come letta
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
