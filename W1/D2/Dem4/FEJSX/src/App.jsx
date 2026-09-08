import { useCallback, useState } from 'react'
import { Campanella } from './Campanella'
import { PannelloConfronto } from './PannelloConfronto'
import { PannelloStomp } from './PannelloStomp'
import { PannelloTrasporto } from './PannelloTrasporto'
import {
  impostaModalitaWs,
  leggiModalitaWs,
  leggiTrasportoScelto,
  salvaTrasportoScelto,
} from './trasporto'
import { useNotifiche } from './useNotifiche'

const UTENTI = ['mario', 'lucia']

/**
 * La pagina della Dem 4 e' un banco di prova, non un'applicazione finita.
 * Tre pannelli, tre cose da guardare:
 *
 *   PannelloConfronto  lo stesso messaggio mandato in modi diversi,
 *                      con il cronometro e il contatore delle richieste HTTP
 *   PannelloTrasporto  cosa succede quando il WebSocket non passa
 *   PannelloStomp      i frame STOMP grezzi: CONNECT, SUBSCRIBE, SEND, MESSAGE
 *
 * Tutte le impostazioni vivono qui e scendono nell'hook come opzioni: cosi' si
 * vede bene quali di esse fanno riaprire la connessione (utente, trasporto, login)
 * e quali invece mandano soltanto un frame (le destinazioni).
 */
export default function App() {
  const [utente, setUtente] = useState(UTENTI[0])
  // In TSX i valori ammessi per queste due erano scritti nei tipi ModoRicezione e
  // TrasportoScelto, importati da useNotifiche e da trasporto. Qui sono stringhe:
  // 'stomp' | 'polling' per il modo, 'auto' | 'websocket' | 'xhr-streaming' |
  // 'xhr-polling' per il trasporto.
  const [modoRicezione, setModoRicezione] = useState('stomp')
  const [senzaLogin, setSenzaLogin] = useState(false)
  const [destinazioni, setDestinazioni] = useState([])

  // Queste due sopravvivono al ricaricamento causato dal tasto "Stacca WebSocket",
  // perche' stanno in sessionStorage. La funzione passata a useState viene eseguita
  // una volta sola, al primo render: e' il valore iniziale letto da li'.
  const [trasportoScelto, setTrasportoScelto] = useState(leggiTrasportoScelto)
  const [modalitaWs] = useState(leggiModalitaWs)

  const stomp = useNotifiche(utente, {
    trasporto: trasportoScelto,
    modoRicezione,
    senzaLogin,
    destinazioni,
  })

  const aggiungi = useCallback((destinazione) => {
    setDestinazioni((precedenti) =>
      precedenti.includes(destinazione) ? precedenti : [...precedenti, destinazione],
    )
  }, [])

  const togli = useCallback((destinazione) => {
    setDestinazioni((precedenti) => precedenti.filter((d) => d !== destinazione))
  }, [])

  const cambiaTrasporto = useCallback((t) => {
    salvaTrasportoScelto(t)
    setTrasportoScelto(t)
  }, [])

  /**
   * I topic su cui si puo' scrivere sono quelli a cui siamo iscritti, e li sappiamo
   * perche' le SUBSCRIBE le abbiamo fatte noi.
   *
   * Attenzione pero': il server non si fida di questo elenco. Ha il suo
   * (SubscriptionRegistry) e ricontrolla. Questo serve solo a costruire la tendina.
   */
  const topicSottoscritti = destinazioni
    .filter((d) => d.startsWith('/topic/'))
    .map((d) => d.slice('/topic/'.length))

  return (
    <main>
      <header>
        <select value={utente} onChange={(e) => setUtente(e.target.value)}>
          {UTENTI.map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>

        {/* Tre indicatori sempre sott'occhio: lo stato della connessione,
            il trasporto che SockJS ha negoziato, e come stiamo ricevendo. */}
        <span className={`stato ${stomp.stato.replace(' ', '-')}`}>{stomp.stato}</span>
        <span className="pill">{stomp.trasportoNegoziato ?? 'nessun trasporto'}</span>
        <span className="pill">{modoRicezione}</span>

        <Campanella
          notifiche={stomp.notifiche}
          nonLette={stomp.nonLette}
          onApri={stomp.segnaLette}
        />
      </header>

      <div className="pannelli">
        <PannelloConfronto
          utente={utente}
          utenti={UTENTI}
          topicSottoscritti={topicSottoscritti}
          modoRicezione={modoRicezione}
          onModoRicezione={setModoRicezione}
          richieste={stomp.richieste}
          scoperta={stomp.scoperta}
          errore={stomp.errore}
          onInviaHttpSenzaPush={stomp.inviaHttpSenzaPush}
          onInviaHttpConPush={stomp.inviaHttpConPush}
          onInviaViaStomp={stomp.inviaViaStomp}
          onInviaSuTopic={stomp.inviaSuTopic}
          onControlla={stomp.ricarica}
        />

        <PannelloTrasporto
          stato={stomp.stato}
          trasportoNegoziato={stomp.trasportoNegoziato}
          trasportoScelto={trasportoScelto}
          onTrasportoScelto={cambiaTrasporto}
          modalitaWs={modalitaWs}
          onModalitaWs={impostaModalitaWs}
          onSimulaCaduta={stomp.simulaCaduta}
        />

        <PannelloStomp
          utente={utente}
          destinazioni={destinazioni}
          onAggiungi={aggiungi}
          onTogli={togli}
          senzaLogin={senzaLogin}
          onSenzaLogin={setSenzaLogin}
          onPing={stomp.inviaPing}
          arrivi={stomp.arrivi}
          righe={stomp.righe}
          onPulisci={stomp.pulisciLog}
        />
      </div>
    </main>
  )
}
