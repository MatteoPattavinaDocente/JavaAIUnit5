import { webSocketManomesso } from './trasporto'

// In TSX questo file importava tre tipi (StatoConnessione, ModalitaWs,
// TrasportoScelto) e dichiarava le sette props. Senza TypeScript resta un solo
// import, quello della funzione che serve davvero a runtime.
//
// I valori che circolano qui sono comunque questi:
//   stato              'connesso' | 'in riconnessione' | 'offline'
//   modalitaWs         'normale' | 'websocket-rotto'
//   trasportoScelto    'auto' | 'websocket' | 'xhr-streaming' | 'xhr-polling'

const TRASPORTI = [
  { valore: 'auto', etichetta: 'auto (WebSocket per primo)' },
  { valore: 'websocket', etichetta: 'solo websocket' },
  { valore: 'xhr-streaming', etichetta: 'solo xhr-streaming' },
  { valore: 'xhr-polling', etichetta: 'solo xhr-polling' },
]

/**
 * Il pannello del trasporto: tre esperimenti, in ordine di importanza.
 *
 *   1. Staccare il WebSocket -> SockJS ripiega su HTTP da solo.
 *      E' il caso vero, quello del proxy aziendale che rifiuta l'upgrade.
 *   2. Forzare un trasporto  -> nessun ripiego, siamo noi a scegliere.
 *   3. Simulare una caduta   -> il client STOMP riconnette e rifa' le iscrizioni,
 *      senza una riga di codice nostro.
 *
 * La differenza fra 1 e 2 e' la parte che a lezione si confonde sempre:
 * la prima e' "non funziona, arrangiati", la seconda e' "non usarlo, te lo dico io".
 * Il perche' dei dettagli tecnici sta in trasporto.js e in index.html.
 */
export function PannelloTrasporto({
  stato,
  trasportoNegoziato,
  trasportoScelto,
  onTrasportoScelto,
  modalitaWs,
  onModalitaWs,
  onSimulaCaduta,
}) {
  const manomesso = modalitaWs !== 'normale'

  return (
    <section className="card">
      <h2>Trasporto</h2>

      <p className="riga-pill">
        <span className={`stato ${stato.replace(' ', '-')}`}>{stato}</span>
        <span className="pill">{trasportoNegoziato ?? 'nessun trasporto'}</span>
      </p>

      {/* Prima leva: si rompe il WebSocket del browser e ripiega SockJS da solo.
          Serve il reload, perche' sockjs-client copia globalThis.WebSocket
          all'import: lo spiega lo script in index.html. */}
      <h3>1. Staccare il WebSocket &mdash; ripiega SockJS</h3>
      <div className="bottoni">
        <button type="button" disabled={manomesso} onClick={() => onModalitaWs('websocket-rotto')}>
          Stacca WebSocket
        </button>
        <button type="button" disabled={!manomesso} onClick={() => onModalitaWs('normale')}>
          Ripristina WebSocket
        </button>
      </div>
      <p className="nota">
        L handshake WebSocket viene dirottato su una porta chiusa: parte e muore, come dietro un
        proxy che rifiuta l upgrade. Nessuno gli dice cosa fare dopo &mdash; ripiega SockJS.
      </p>

      {manomesso && (
        <p className="avviso">
          WebSocket staccato
          {webSocketManomesso() ? ' (patch attiva)' : ' (patch NON attiva: ricarica la pagina)'}.
          <br />
          Nel Network, filtro WS: una riga rossa verso <code>ws://localhost:59999</code>, e subito
          dopo, in Fetch/XHR, <code>xhr_streaming</code> che resta pending piu una{' '}
          <code>xhr_send</code> per ogni frame in uscita. In console il browser si lamenta della
          connessione fallita: e voluto. Le notifiche arrivano lo stesso, e il codice STOMP non e
          cambiato di una riga.
        </p>
      )}

      {/* Seconda leva: nessuno ripiega, gli si dice quale trasporto usare.
          Non serve il reload, basta rifare la connessione. */}
      <h3>2. Forzare il trasporto &mdash; opzione di SockJS</h3>
      <label>
        {/* In TSX il valore della select andava convertito con "as TrasportoScelto":
            per il compilatore e' una stringa qualunque. Qui passa cosi' com'e', e
            se scrivessimo male il nome di un trasporto lo scopriremmo solo vedendo
            la connessione fallire. */}
        <select value={trasportoScelto} onChange={(e) => onTrasportoScelto(e.target.value)}>
          {TRASPORTI.map((t) => (
            <option key={t.valore} value={t.valore}>
              {t.etichetta}
            </option>
          ))}
        </select>
      </label>
      <p className="nota">
        <code>new SockJS(url, null, {'{'} transports: ['{trasportoScelto}'] {'}'})</code>
        {trasportoScelto === 'auto' && ' -- cioe nessuna opzione: SockJS decide lui.'}
        {trasportoScelto === 'websocket' &&
          ' -- solo WebSocket: se non si apre, SockJS non ha alternative e la connessione fallisce. Con il tasto qui sopra insieme a questa scelta, si vede il caso senza ripiego.'}
        {trasportoScelto === 'xhr-streaming' &&
          ' -- qui il WebSocket non viene nemmeno tentato: filtro WS, zero righe. Non e un ripiego, e una scelta.'}
        {trasportoScelto === 'xhr-polling' &&
          ' -- long polling puro: una richiesta che si chiude e ne riapre subito un altra, all infinito. Il costo si conta a occhio nel Network.'}
      </p>

      <h3>3. La caduta</h3>
      <div className="bottoni">
        <button type="button" onClick={onSimulaCaduta}>
          Simula caduta di rete
        </button>
      </div>
      <p className="nota">
        <code>client.forceDisconnect()</code>: lo stato passa a <em>in riconnessione</em>, e dopo 5
        secondi (<code>reconnectDelay</code>) il client riapre e <code>onConnect</code> rifa tutte
        le SUBSCRIBE. Nessuna riga di codice nostro per la riconnessione.
      </p>
    </section>
  )
}
