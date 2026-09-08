import { AreaFrame } from './AreaFrame'
import { useCanale } from './useCanale'

/**
 * Tutta la Dem 2 sta in questa pagina: uno stato (connesso o no), un pulsante
 * per mandare qualcosa, e l'elenco di quello che torna indietro.
 *
 * L'hook viene chiamato da un solo componente, ed e' voluto: cosi' esiste una
 * sola connessione per tutta l'applicazione. Se lo chiamassero due componenti
 * diversi si aprirebbero due socket, e ogni messaggio arriverebbe due volte.
 */
export default function App() {
  const { connesso, messaggi, invia, svuota } = useCanale()

  return (
    <main>
      <h1>Canale WebSocket</h1>

      <div className="barra">
        <span className={connesso ? 'stato connesso' : 'stato'}>
          {connesso ? 'connesso' : 'non connesso'}
        </span>

        <button onClick={() => invia('ping ' + new Date().toLocaleTimeString('it-IT'))}>
          Invia sul canale
        </button>
      </div>

      <AreaFrame messaggi={messaggi} onSvuota={svuota} />

      <p className="nota">
        Un solo indirizzo, <code>ws://localhost:8080/ws</code>, e nessuna destinazione: quello
        che arriva al server e' una stringa, e quello che il server rimanda lo ricevono tutte
        le sessioni aperte, compresa quella di chi ha premuto il pulsante. Chi tiene l'elenco
        delle sessioni e' codice nostro.
      </p>

      <p className="nota">
        Da provare: apri questa pagina in due schede e premi il pulsante in una sola. Il
        messaggio compare in tutte e due. Poi spegni il backend e guarda la console del
        browser: la connessione cade e riparte da sola ogni 5 secondi, perche' quella
        riconnessione l'abbiamo scritta noi dentro <code>useCanale</code>.
      </p>
    </main>
  )
}
