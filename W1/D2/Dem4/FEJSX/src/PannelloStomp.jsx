import { useState } from 'react'
import { DESTINAZIONE_PERSONALE } from './useNotifiche'

// In TSX l'import portava anche i tipi Arrivo e Riga, e le dieci props erano
// dichiarate in un type Props. Restano qui come promemoria delle due forme che
// questo pannello disegna:
//
//   arrivi  { id, destinazione, corpo, ora }
//   righe   { id, testo, verso }  con verso 'in' | 'out' | 'nota'

// Le destinazioni che il backend usa davvero, per non doverle ricordare a lezione.
const PRONTE = [
  { destinazione: '/topic/notifications', nota: 'broadcast: a tutti i connessi, nessun filtro' },
  { destinazione: '/topic/test', nota: 'dove @SendTo rimette il ritorno di /app/ping' },
]

// Gli heart-beat sono la meta' delle righe del log e non dicono niente di nuovo.
const battito = (testo) => testo.includes('PING') || testo.includes('PONG')

/**
 * Il pannello dei frame STOMP: qui si guarda il protocollo, non l'applicazione.
 *
 * Un riquadro per ogni comando del protocollo — CONNECT, SUBSCRIBE/UNSUBSCRIBE,
 * SEND — piu' l'elenco dei messaggi arrivati e il log dei frame grezzi.
 *
 * Il riquadro piu' utile e' l'ultimo: quei frame sono esattamente le stringhe che
 * passano nel tubo. Nella Dem 2 e nella Dem 3 quelle stringhe le inventavamo noi;
 * qui hanno un formato che chiunque riconosce, e infatti c'e' una libreria che le
 * legge al posto nostro.
 */
export function PannelloStomp({
  utente,
  destinazioni,
  onAggiungi,
  onTogli,
  senzaLogin,
  onSenzaLogin,
  onPing,
  arrivi,
  righe,
  onPulisci,
}) {
  const [ordine, setOrdine] = useState('42')
  const [libera, setLibera] = useState('')
  const [testoPing, setTestoPing] = useState('ciao')
  const [mostraBattiti, setMostraBattiti] = useState(false)

  const visibili = mostraBattiti ? righe : righe.filter((r) => !battito(r.testo))

  return (
    <>
      <section className="card">
        <h2>CONNECT</h2>
        <label>
          <input
            type="checkbox"
            checked={senzaLogin}
            onChange={(e) => onSenzaLogin(e.target.checked)}
          />
          CONNECT senza header <code>login</code>
        </label>
        <p className="nota">
          Con l header, <code>StompLoginInterceptor</code> mette un Principal sulla sessione e{' '}
          <code>convertAndSendToUser</code> trova dove consegnare. Senza, il log del backend scrive{' '}
          <em>CONNECT senza header login</em>, la sessione resta ANONIMA e le notifiche personali
          vengono scartate <strong>in silenzio</strong>: nessun errore, nessun frame, solo il badge
          che non si accende. Il broadcast su <code>/topic</code> arriva comunque.
        </p>
      </section>

      <section className="card">
        <h2>SUBSCRIBE / UNSUBSCRIBE</h2>

        <p className="nota">
          Sempre attiva: <code>{DESTINAZIONE_PERSONALE}</code> &mdash; <code>/user</code> e un
          prefisso virtuale, Spring lo riscrive nella coda privata di questa sessione.
        </p>

        {PRONTE.map(({ destinazione, nota }) => (
          <label key={destinazione}>
            <input
              type="checkbox"
              checked={destinazioni.includes(destinazione)}
              onChange={(e) =>
                e.target.checked ? onAggiungi(destinazione) : onTogli(destinazione)
              }
            />
            <code>{destinazione}</code> <small>{nota}</small>
          </label>
        ))}

        <div className="bottoni">
          <input
            value={ordine}
            onChange={(e) => setOrdine(e.target.value)}
            size={5}
            aria-label="id ordine"
          />
          <button type="button" onClick={() => onAggiungi(`/topic/ordini/${ordine}`)}>
            SUBSCRIBE /topic/ordini/{ordine || '?'}
          </button>
        </div>
        <p className="nota">
          Il broker non ha nessun elenco di destinazioni: <code>/topic/ordini/7</code> esiste perche
          qualcuno ci si e sottoscritto. Nella Dem 3 questo era un comando inventato da noi piu un
          insieme di id tenuto per ogni sessione.
        </p>
        <p className="nota">
          Ogni topic sottoscritto qui diventa anche un topic su cui si puo <strong>scrivere</strong>
          , dal pannello dei messaggi. Il server tiene il suo elenco in{' '}
          <code>SubscriptionRegistry</code>, alimentato dagli eventi{' '}
          <code>SessionSubscribeEvent</code> e <code>SessionUnsubscribeEvent</code>: la SUBSCRIBE e
          l unico modo di guadagnarsi il permesso, e la UNSUBSCRIBE lo toglie subito.
        </p>

        <div className="bottoni">
          <input
            value={libera}
            onChange={(e) => setLibera(e.target.value)}
            placeholder="/topic/qualunque"
            size={24}
            aria-label="destinazione libera"
          />
          <button
            type="button"
            disabled={!libera.startsWith('/')}
            onClick={() => {
              onAggiungi(libera)
              setLibera('')
            }}
          >
            SUBSCRIBE
          </button>
        </div>

        {destinazioni.length > 0 && (
          <ul className="destinazioni">
            {destinazioni.map((d) => (
              <li key={d}>
                <code>{d}</code>
                <button type="button" onClick={() => onTogli(d)}>
                  UNSUBSCRIBE
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="card">
        <h2>SEND</h2>
        <div className="bottoni">
          <input
            value={testoPing}
            onChange={(e) => setTestoPing(e.target.value)}
            size={16}
            aria-label="testo del ping"
          />
          <button type="button" onClick={() => onPing(testoPing)}>
            SEND /app/ping
          </button>
        </div>
        <p className="nota">
          <code>/app</code> non e un prefisso del broker: porta a <code>@MessageMapping("/ping")</code>,
          cioe a codice nostro. Il valore restituito lo rimette <code>@SendTo("/topic/test")</code>{' '}
          sul broker, quindi il giro si chiude solo se <code>/topic/test</code> e fra le
          sottoscrizioni qui sopra.
        </p>
      </section>

      <section className="card">
        <h2>Arrivi ({arrivi.length})</h2>
        {arrivi.length === 0 && <p className="nota">niente ancora</p>}
        <ul className="arrivi">
          {arrivi.map((a) => (
            <li key={a.id}>
              <code>{a.destinazione}</code>
              <small>{a.ora}</small>
              <pre>{a.corpo}</pre>
            </li>
          ))}
        </ul>
        <p className="nota">
          La destinazione qui sopra la leggiamo da <code>frame.headers.destination</code>: sta negli
          header del frame MESSAGE, non dentro il payload. Il corpo e solo il DTO.
        </p>
      </section>

      <section className="card">
        <h2>Frame grezzi</h2>
        <div className="bottoni">
          <label>
            <input
              type="checkbox"
              checked={mostraBattiti}
              onChange={(e) => setMostraBattiti(e.target.checked)}
            />
            mostra gli heart-beat
          </label>
          <button type="button" onClick={onPulisci}>
            pulisci
          </button>
        </div>
        <pre className="log">
          {visibili.map((r) => (
            <span key={r.id} className={`riga-${r.verso}`}>
              {r.testo}
              {'\n'}
            </span>
          ))}
        </pre>
      </section>

      <section className="card">
        <h2>Da terminale</h2>
        <pre className="log">
          {[
            `# notifica personale a ${utente} (/user/queue/notifications)`,
            `curl -X POST "http://localhost:8080/api/demo/orders/42/ship?recipient=${utente}"`,
            '',
            '# lo stesso messaggio, senza e con push: la differenza sta solo nella consegna',
            `curl -X POST "http://localhost:8080/api/demo/messaggi/senza-push?from=lucia&to=${utente}&text=ciao"`,
            `curl -X POST "http://localhost:8080/api/demo/messaggi/con-push?from=lucia&to=${utente}&text=ciao"`,
            '',
            '# broadcast su /topic/test, senza nessuna sessione WebSocket in corso',
            'curl -X POST "http://localhost:8080/api/demo/broadcast?text=ciao"',
            '',
            '# la notifica 1, ripubblicata sulle altre due destinazioni',
            'curl -X POST "http://localhost:8080/api/demo/notifications/1/broadcast"',
            'curl -X POST "http://localhost:8080/api/demo/notifications/1/to-order"',
          ].join('\n')}
        </pre>
      </section>
    </>
  )
}
