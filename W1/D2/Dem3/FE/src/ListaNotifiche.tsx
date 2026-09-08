import type { NotificationDto, Pagina } from './api'
import { oraEsatta, quandoFa, TIPI } from './tipi'

type Props = {
  utente: string
  pagina: Pagina<NotificationDto>
  dimensione: number
  caricamento: boolean
  arretrate: number
  onVaiA: (page: number) => void
  onCambiaDimensione: (size: number) => void
  onSegnaUna: (id: number) => Promise<void>
  onSegnaTutte: () => Promise<void>
}

const DIMENSIONI = [5, 10, 20]

/**
 * Lo storico delle notifiche dell'utente, con la paginazione.
 *
 * Questi dati arrivano dalle chiamate HTTP, non dal canale: sono la versione
 * ufficiale, quella salvata sul database. Il canale serve solo a farci sapere
 * che c'e' qualcosa di nuovo da rileggere.
 */
export function ListaNotifiche({
  utente,
  pagina,
  dimensione,
  caricamento,
  arretrate,
  onVaiA,
  onCambiaDimensione,
  onSegnaUna,
  onSegnaTutte,
}: Props) {
  const { content, number, totalElements, totalPages, first, last } = pagina

  // "da 6 a 10 di 37": i numeri per la scritta in fondo.
  // number parte da 0, quindi la prima notifica della pagina 1 e' la numero 6 quando
  // ce ne sono 5 per pagina.
  const da = totalElements === 0 ? 0 : number * dimensione + 1
  const a = number * dimensione + content.length

  return (
    <section className="scheda lista">
      <header className="scheda-testa">
        <h2>Notifiche di {utente}</h2>
        <div className="azioni">
          <label>
            per pagina
            <select
              value={dimensione}
              onChange={(e) => onCambiaDimensione(Number(e.target.value))}
            >
              {DIMENSIONI.map((d) => (
                <option key={d} value={d}>
                  {d}
                </option>
              ))}
            </select>
          </label>
          <button className="secondario" onClick={() => void onSegnaTutte()}>
            segna tutte lette
          </button>
        </div>
      </header>

      {/* Compare solo se sono arrivate notifiche mentre l'utente guardava una pagina
          diversa dalla prima. Invece di spostargli le righe sotto il dito, gli
          diciamo che ci sono novita' e lo lasciamo decidere. */}
      {arretrate > 0 && (
        <button className="avviso" onClick={() => onVaiA(0)}>
          {arretrate} {arretrate === 1 ? 'nuova notifica' : 'nuove notifiche'} — vai alla prima
          pagina
        </button>
      )}

      <ul className={caricamento ? 'righe in-caricamento' : 'righe'}>
        {content.length === 0 && !caricamento && (
          <li className="vuoto">nessuna notifica su questa pagina</li>
        )}
        {content.map((n) => (
          <li key={n.id} className={n.read ? 'riga letta' : 'riga'}>
            <span className={`pallino ${TIPI[n.type].classe}`}>{TIPI[n.type].icona}</span>

            <div className="corpo">
              <strong>{n.title}</strong>
              <small>
                <span className={`tag ${TIPI[n.type].classe}`}>{TIPI[n.type].etichetta}</span>
                {n.resourceId !== null && <span className="tag neutro">#{n.resourceId}</span>}
                {/* "3 minuti fa" da leggere, la data completa passando col mouse. */}
                <time dateTime={n.createdAt} title={oraEsatta(n.createdAt)}>
                  {quandoFa(n.createdAt)}
                </time>
              </small>
            </div>

            {!n.read && (
              <button className="minimo" onClick={() => void onSegnaUna(n.id)}>
                segna letta
              </button>
            )}
          </li>
        ))}
      </ul>

      <footer className="pager">
        <span className="conteggio">
          {totalElements === 0 ? 'nessun risultato' : `${da}–${a} di ${totalElements}`}
        </span>

        {/* first e last arrivano dal backend: e' lui a sapere se ci sono altre pagine,
            e disabilitiamo i pulsanti di conseguenza invece di calcolarlo qui. */}
        <div className="bottoni">
          <button disabled={first || caricamento} onClick={() => onVaiA(0)}>
            «
          </button>
          <button disabled={first || caricamento} onClick={() => onVaiA(number - 1)}>
            ‹ prec
          </button>
          <span className="corrente">
            pagina {totalPages === 0 ? 0 : number + 1} di {totalPages}
          </span>
          <button disabled={last || caricamento} onClick={() => onVaiA(number + 1)}>
            succ ›
          </button>
          <button disabled={last || caricamento} onClick={() => onVaiA(totalPages - 1)}>
            »
          </button>
        </div>
      </footer>
    </section>
  )
}
