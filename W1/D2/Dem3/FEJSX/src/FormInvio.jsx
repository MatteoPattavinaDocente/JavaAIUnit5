import { useState } from 'react'
import { inviaNotifica } from './api'
import { TIPI, TIPI_ORDINATI } from './tipi'

// In TSX le props erano dichiarate in un type Props = { mittente: string; destinatario: string }.

// Se l'utente non scrive un titolo, lo componiamo noi in base al tipo scelto.
// In TSX era un Record<NotificationType, (id: string) => string>: obbligava a
// coprire tutti i tipi. Qui e' un oggetto di funzioni, e un tipo dimenticato
// diventerebbe un errore a tempo di esecuzione, "TITOLI[tipo] is not a function".
const TITOLI = {
  ORDER_SHIPPED: (id) => `Ordine ${id} spedito`,
  ORDER_CANCELLED: (id) => `Ordine ${id} annullato`,
  MESSAGE: () => 'Nuovo messaggio',
}

/**
 * Il modulo per creare una notifica destinata all'altro utente.
 *
 * DA NOTARE, perche' e' il punto della demo: questo form fa una normale POST HTTP.
 * Non tocca la WebSocket. E' il backend che, dopo aver salvato la notifica,
 * la spedisce sul canale al destinatario.
 *
 * Il percorso completo e':
 *   browser --POST--> controller --> service (salva + annuncia)
 *          --> listener (dopo il commit) --> handler --> WebSocket dell'altro utente
 */
export function FormInvio({ mittente, destinatario }) {
  const [tipo, setTipo] = useState('ORDER_SHIPPED')
  const [ordine, setOrdine] = useState('42')
  const [titolo, setTitolo] = useState('')
  const [invio, setInvio] = useState(false)
  // L'esito e' null finche' non si spedisce, poi diventa { ok, testo }.
  const [esito, setEsito] = useState(null)

  const titoloFinale = titolo.trim() || TITOLI[tipo](ordine || '—')

  const spedisci = async (e) => {
    // Senza questo, il browser ricaricherebbe l'intera pagina come si faceva
    // con i form HTML classici, e perderemmo tutto lo stato React.
    e.preventDefault()

    setInvio(true)
    setEsito(null)
    try {
      await inviaNotifica({
        recipient: destinatario,
        type: tipo,
        resourceId: ordine.trim() === '' ? null : Number(ordine),
        title: titoloFinale,
      })
      setEsito({ ok: true, testo: `inviata a ${destinatario}` })
      setTitolo('')
    } catch (err) {
      setEsito({ ok: false, testo: err instanceof Error ? err.message : String(err) })
    } finally {
      // Il pulsante va riabilitato in ogni caso, anche dopo un errore.
      setInvio(false)
    }
  }

  return (
    <section className="scheda invio">
      <header className="scheda-testa">
        <h2>Invia notifica</h2>
      </header>

      <p className="rotta">
        <span className="chi">{mittente}</span>
        <span className="freccia">→</span>
        <span className="chi destinatario">{destinatario}</span>
      </p>
      <p className="nota">
        Il mittente non riceve mai la propria notifica: cambia utente in alto per vedere
        l'altro lato della coda.
      </p>

      <form onSubmit={spedisci}>
        <label>
          tipo
          {/* In TSX qui c'era un "as NotificationType": il valore di una select e'
              una stringa qualunque, e andava promesso al compilatore che fosse uno
              dei tre tipi. Qui e' una stringa e nessuno controlla che sia giusta. */}
          <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
            {TIPI_ORDINATI.map((t) => (
              <option key={t} value={t}>
                {TIPI[t].icona} {TIPI[t].etichetta}
              </option>
            ))}
          </select>
        </label>

        <label>
          id risorsa
          <input
            type="number"
            value={ordine}
            min={1}
            onChange={(e) => setOrdine(e.target.value)}
            placeholder="42"
          />
        </label>

        <label className="lungo">
          titolo
          <input
            value={titolo}
            onChange={(e) => setTitolo(e.target.value)}
            placeholder={TITOLI[tipo](ordine || '—')}
          />
        </label>

        <button className="primario" disabled={invio}>
          {invio ? 'invio…' : `invia a ${destinatario}`}
        </button>
      </form>

      {esito && <p className={esito.ok ? 'esito ok' : 'esito ko'}>{esito.testo}</p>}
    </section>
  )
}
