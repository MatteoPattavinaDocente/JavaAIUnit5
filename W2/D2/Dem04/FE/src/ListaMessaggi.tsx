import { memo, useEffect, useRef } from 'react'
import type { MessaggioRisposta } from './api'

/**
 * Quante volte ogni riga e' stata disegnata. E' strumentazione da lezione, non
 * codice da progetto: il modo giusto di misurare i ridisegni e' il profiler di
 * React (slide 45). Sta fuori dal componente perche' deve sopravvivere ai
 * ridisegni senza essere uno stato.
 */
const conteggioDisegni = new Map<string, number>()

type PropsRiga = {
  messaggio: MessaggioRisposta
  mio: boolean
}

/**
 * React.memo evita il ridisegno delle righe il cui contenuto non e' cambiato
 * (slide 43). Senza, ogni messaggio in arrivo ridisegna TUTTA la lista, non il
 * solo elemento aggiunto.
 *
 * Perche' funzioni le props devono essere stabili: `messaggio` e' lo stesso
 * oggetto finche' non cambia davvero, e `mio` e' un booleano. Se qui passassimo
 * una funzione ricreata a ogni ridisegno, memo non servirebbe a niente.
 */
export const Riga = memo(function Riga({ messaggio, mio }: PropsRiga) {
  const chiave = String(messaggio.id ?? messaggio.idTemporaneo)
  const disegni = (conteggioDisegni.get(chiave) ?? 0) + 1
  conteggioDisegni.set(chiave, disegni)

  return (
    <li className={mio ? 'mio' : 'altrui'}>
      <p>{messaggio.testo}</p>
      <span className="meta">
        {new Date(messaggio.istante).toLocaleTimeString('it-IT')}
        {mio && <> · {etichetta(messaggio.stato)}</>}
        <span className="disegni"> · disegni: {disegni}</span>
      </span>
    </li>
  )
})

function etichetta(stato: string) {
  switch (stato) {
    case 'IN INVIO':
      return 'in invio…'
    case 'INVIATO':
      return 'inviato'
    case 'CONSEGNATO':
      return 'consegnato ✓'
    case 'LETTO':
      return 'letto ✓✓'
    default:
      return stato
  }
}

type Props = {
  messaggi: MessaggioRisposta[]
  utente: string
}

export function ListaMessaggi({ messaggi, utente }: Props) {
  const listaRef = useRef<HTMLUListElement>(null)
  // «Era in fondo» va misurato PRIMA dell'inserimento, non dopo.
  const eraInFondo = useRef(true)

  useEffect(() => {
    const lista = listaRef.current
    if (!lista) return
    // La lista scende in fondo solo se l'utente era gia' in fondo prima
    // dell'arrivo: altrimenti gli si strappa via quello che stava leggendo
    // (slide 37).
    if (eraInFondo.current) {
      lista.scrollTop = lista.scrollHeight
    }
  }, [messaggi])

  function ricordaPosizione() {
    const lista = listaRef.current
    if (!lista) return
    eraInFondo.current = lista.scrollHeight - lista.scrollTop - lista.clientHeight < 40
  }

  return (
    <ul className="messaggi" ref={listaRef} onScroll={ricordaPosizione}>
      {messaggi.length === 0 && <li className="vuoto">nessun messaggio</li>}
      {messaggi.map((m) => (
        // La chiave e' l'identificativo del messaggio, non la posizione
        // nell'array: con l'indice React riordina le righe a ogni inserimento
        // (slide 40 e 46).
        <Riga key={m.id ?? m.idTemporaneo!} messaggio={m} mio={m.mittente === utente} />
      ))}
    </ul>
  )
}
