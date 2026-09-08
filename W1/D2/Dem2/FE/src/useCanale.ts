import { useEffect, useRef, useState } from 'react'

// ws:// invece di http://: e' lo schema delle WebSocket.
// Stessa porta e stesso server di Spring, indirizzo /ws come da WebSocketConfig.
const WS_URL = 'ws://localhost:8080/ws'
const RITARDO_RICONNESSIONE = 5000 // millisecondi di attesa prima di riprovare

// La forma del messaggio che ci manda il backend (il record TestMessage in Java).
export type TestMessage = { text: string }

// Quello che teniamo in pagina per ogni messaggio arrivato.
// Mostriamo anche la stringa grezza per far vedere cosa viaggia davvero nel tubo:
// una WebSocket nuda non consegna una destinazione o un mittente, consegna solo questo.
export type MessaggioRicevuto = {
  testo: string
  grezzo: string
  ricevutoAlle: string
}

/**
 * Apre la connessione WebSocket e tiene in pagina i messaggi che arrivano.
 *
 * Non usiamo nessuna libreria: WebSocket e' una classe che il browser ha gia'.
 * Tutto quello che si vede qui dentro (riconnessione, controllo dello stato,
 * lettura del JSON) e' lavoro che dobbiamo fare noi. Nella Dem 4 lo fara' il client STOMP.
 */
export function useCanale() {
  const [connesso, setConnesso] = useState(false)
  const [messaggi, setMessaggi] = useState<MessaggioRicevuto[]>([])

  // useRef e' una "scatola" che sopravvive ai render senza farne scattare di nuovi.
  // Qui ci teniamo la socket per poterla usare dentro invia(), che sta fuori dall'useEffect.
  const socketRef = useRef<WebSocket | null>(null)

  // L'array vuoto [] alla fine significa: esegui questo effetto una volta sola,
  // quando il componente compare. Una connessione per tutta l'applicazione.
  useEffect(() => {
    let socket: WebSocket
    let timer: number | undefined
    let chiusuraVoluta = false // per distinguere "ho chiuso io" da "e' caduta"

    const apri = () => {
      socket = new WebSocket(WS_URL)
      socketRef.current = socket

      // Il collegamento e' pronto: da adesso si puo' scrivere.
      socket.onopen = () => {
        console.log('[WS] aperta', WS_URL)
        setConnesso(true)
      }

      // E' arrivato qualcosa dal server.
      socket.onmessage = (evento) => {
        const grezzo = evento.data as string
        console.log('[WS] ricevuto', grezzo)

        // Il payload e' sempre e solo una stringa: che sia JSON e' una convenzione
        // nostra, non una regola del protocollo. Quindi il parse puo' fallire, e senza
        // il try qui sotto un solo messaggio malformato romperebbe la pagina.
        let corpo: TestMessage
        try {
          corpo = JSON.parse(grezzo) as TestMessage
        } catch {
          console.warn('[WS] messaggio non in formato JSON, ignorato')
          return
        }

        // Il nuovo messaggio va in cima: lo spread ...precedenti ricopia i vecchi dopo di lui.
        // Creiamo un array nuovo invece di modificare quello vecchio, altrimenti React
        // non si accorge del cambiamento e non ridisegna niente.
        setMessaggi((precedenti) => [
          { testo: corpo.text, grezzo, ricevutoAlle: new Date().toLocaleTimeString('it-IT') },
          ...precedenti,
        ])
      }

      socket.onerror = (evento) => console.error('[WS] errore', evento)

      // La connessione si e' chiusa: server riavviato, rete caduta, o siamo stati noi.
      // Se non e' stata una nostra scelta riproviamo fra qualche secondo.
      // Una WebSocket caduta NON si riapre da sola: senza questa riga la pagina
      // resterebbe muta per sempre, e sembrerebbe un bug del backend.
      socket.onclose = (evento) => {
        console.log('[WS] chiusa code=' + evento.code)
        setConnesso(false)
        if (!chiusuraVoluta) timer = window.setTimeout(apri, RITARDO_RICONNESSIONE)
      }
    }

    apri()

    // La funzione restituita da useEffect e' la pulizia: React la chiama quando il
    // componente sparisce. Senza, in StrictMode (che monta due volte) resterebbero due
    // connessioni aperte e ogni messaggio comparirebbe in pagina due volte.
    return () => {
      chiusuraVoluta = true
      window.clearTimeout(timer)
      socket.close()
    }
  }, [])

  const invia = (text: string) => {
    const socket = socketRef.current

    // Scrivere su una socket non ancora aperta lancia un errore: lo stato lo dobbiamo
    // controllare noi ogni volta. Un client STOMP invece accoderebbe il messaggio
    // e lo spedirebbe da solo appena la connessione e' pronta.
    if (socket?.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ text }))
    } else {
      console.warn('[WS] non aperta: messaggio non inviato')
    }
  }

  const svuota = () => setMessaggi([])

  return { connesso, messaggi, invia, svuota }
}
