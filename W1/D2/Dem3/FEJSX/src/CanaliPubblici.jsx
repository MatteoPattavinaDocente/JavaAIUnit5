import { useState } from 'react'
import { TIPI } from './tipi'

// In TSX questo componente apriva con l'elenco delle props e delle loro firme:
//
//   type Props = {
//     pubbliche: BustaRicevuta[]
//     ordiniSeguiti: number[]
//     onSegui: (ordine: number) => void
//     onSmetti: (ordine: number) => void
//     onPubblica: (ordine: number, testo: string) => boolean
//     onSvuota: () => void
//   }
//
// Era anche una piccola documentazione: si leggeva subito che onPubblica restituisce
// un booleano. Senza tipi quel dettaglio si scopre solo leggendo useNotifiche.

/**
 * Gli altri due canali: quello che va a tutti e quello che va a chi segue un ordine.
 *
 * E' la parte che con STOMP non si scriverebbe proprio. Qui "seguire un ordine"
 * significa mandare al server un comando inventato da noi
 * ({"azione":"segui","ordine":42}) e fidarsi che se lo ricordi.
 * Nella Dem 4 diventa una riga sola: client.subscribe('/topic/orders/42', ...).
 */
export function CanaliPubblici({
  pubbliche,
  ordiniSeguiti,
  onSegui,
  onSmetti,
  onPubblica,
  onSvuota,
}) {
  const [ordine, setOrdine] = useState('42')
  const [testo, setTesto] = useState('')

  const segui = (e) => {
    e.preventDefault()
    const numero = Number(ordine)
    // Number('ciao') restituisce NaN, non un errore: il controllo tocca a noi.
    if (Number.isFinite(numero) && numero > 0) onSegui(numero)
  }

  // null = non abbiamo ancora scelto un canale su cui scrivere.
  const [destinazione, setDestinazione] = useState(null)

  /**
   * Su quale ordine stiamo per scrivere.
   *
   * Se quello scelto non e' piu' fra i seguiti (per esempio abbiamo appena smesso
   * di seguirlo) ripieghiamo sul primo disponibile, e se non ce ne sono restiamo a null.
   *
   * Questo e' un controllo di comodita', non una garanzia: la regola vera la applica
   * il server, che scarta i comandi sugli ordini che questa sessione non segue.
   * Un utente smaliziato potrebbe mandare il comando a mano dalla console del browser.
   */
  const canale =
    destinazione !== null && ordiniSeguiti.includes(destinazione)
      ? destinazione
      : (ordiniSeguiti[0] ?? null)

  const pubblica = (e) => {
    e.preventDefault()
    if (canale === null || testo.trim() === '') return
    // Svuotiamo la casella solo se il messaggio e' partito davvero.
    if (onPubblica(canale, testo.trim())) setTesto('')
  }

  return (
    <section className="scheda">
      <header className="scheda-testa">
        <h2>Canali non personali</h2>
        <div className="azioni">
          <button className="secondario" onClick={onSvuota} disabled={pubbliche.length === 0}>
            svuota
          </button>
        </div>
      </header>

      <form onSubmit={segui}>
        <label>
          segui ordine
          <input
            type="number"
            min={1}
            value={ordine}
            onChange={(e) => setOrdine(e.target.value)}
            placeholder="42"
          />
        </label>
        <button className="primario">segui</button>
      </form>

      <p className="nota">
        {ordiniSeguiti.length === 0
          ? "nessun ordine seguito: /to-order non arrivera'"
          : 'ordini seguiti su questa sessione:'}
        {ordiniSeguiti.map((o) => (
          <button key={o} className="minimo" onClick={() => onSmetti(o)}>
            #{o} &times;
          </button>
        ))}
      </p>

      <form onSubmit={pubblica}>
        <label>
          scrivi su
          <select
            value={canale ?? ''}
            onChange={(e) => setDestinazione(Number(e.target.value))}
            disabled={canale === null}
          >
            {ordiniSeguiti.length === 0 && <option value="">nessun canale</option>}
            {ordiniSeguiti.map((o) => (
              <option key={o} value={o}>
                ordine #{o}
              </option>
            ))}
          </select>
        </label>
        <label className="lungo">
          messaggio
          <input
            value={testo}
            onChange={(e) => setTesto(e.target.value)}
            placeholder={canale === null ? 'segui un ordine per poter scrivere' : 'in arrivo domani'}
            disabled={canale === null}
          />
        </label>
        <button className="primario" disabled={canale === null || testo.trim() === ''}>
          pubblica
        </button>
      </form>

      <ul className="righe">
        {pubbliche.length === 0 && (
          <li className="vuoto">
            niente. Prova: <code>POST /api/demo/notifications/1/broadcast</code> oppure{' '}
            <code>POST /api/demo/notifications/1/to-order</code>
          </li>
        )}
        {pubbliche.map((b, i) => (
          <li key={pubbliche.length - i} className="riga">
            <span className={`pallino ${TIPI[b.notifica.type].classe}`}>
              {TIPI[b.notifica.type].icona}
            </span>
            <div className="corpo">
              <strong>{b.notifica.title}</strong>
              <small>
                {/* Il canale da cui e' arrivata: e' il campo che il backend
                    ci ha messo dentro il contenuto, non un'intestazione. */}
                <span className="tag neutro">{b.canale}</span>

                {/* id a null significa: l'ha scritto un altro utente sul canale,
                    non e' salvata da nessuna parte e nello storico non comparira' mai.
                    Per questo diciamo "da" invece di "a". */}
                <span className="tag neutro">
                  {b.notifica.id === null ? 'da' : 'a'} {b.notifica.recipient}
                </span>
                {b.notifica.id === null && <span className="tag neutro">non salvato</span>}

                <time>{b.ricevutaAlle}</time>
              </small>
            </div>
          </li>
        ))}
      </ul>

      <p className="nota">
        Queste buste arrivano sulla stessa identica connessione delle notifiche personali:
        e' il campo <code>canale</code> dentro il contenuto a dirci perche' le abbiamo
        ricevute. Un messaggio pubblicato da qui non tocca il database: chi non e'
        collegato in quel momento lo perde, e nello storico non c'e'.
      </p>
    </section>
  )
}
