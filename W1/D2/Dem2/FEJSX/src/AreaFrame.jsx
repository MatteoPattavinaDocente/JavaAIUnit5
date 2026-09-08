/**
 * L'elenco dei messaggi arrivati dal canale: una riga per messaggio,
 * il piu' recente in cima.
 *
 * Questo componente non conosce la WebSocket: riceve un array e lo disegna.
 * Separare "chi parla col server" da "chi disegna" e' il motivo per cui
 * la logica sta in useCanale e non qui dentro.
 *
 * In TSX la firma dichiarava anche la forma delle props:
 *   { messaggi, onSvuota }: { messaggi: MessaggioRicevuto[]; onSvuota: () => void }
 * Qui restano solo i due nomi. Che dentro ogni messaggio ci siano testo, grezzo e
 * ricevutoAlle lo sappiamo perche' lo abbiamo scritto in useCanale: nessuno ce lo
 * ricorda mentre scriviamo, e nessuno protesta se ne inventiamo un altro.
 */
export function AreaFrame({ messaggi, onSvuota }) {
  return (
    <section className="area">
      <header>
        <strong>Messaggi ricevuti</strong>
        <span className="conteggio">{messaggi.length}</span>
        <button onClick={onSvuota} disabled={messaggi.length === 0}>
          Svuota
        </button>
      </header>

      {messaggi.length === 0 ? (
        <p className="vuoto">
          Nessun messaggio. Premi il pulsante qui sopra, oppure lancia dal terminale:
          <code>curl -X POST "http://localhost:8080/api/demo/broadcast?text=ciao"</code>
        </p>
      ) : (
        <ul>
          {messaggi.map((m, i) => (
            // I messaggi non hanno un id, quindi la key la ricaviamo dalla posizione.
            // Contiamo dal fondo, cosi' una riga gia' in pagina tiene la sua key
            // anche quando davanti a lei ne arrivano di nuove.
            <li key={messaggi.length - i} className={i === 0 ? 'ultimo' : ''}>
              <span className="ora">{m.ricevutoAlle}</span>
              <span className="testo">{m.testo}</span>
              {/* La stringa esatta arrivata dal tubo, senza interpretazioni. */}
              <span className="destinazione">{m.grezzo}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
