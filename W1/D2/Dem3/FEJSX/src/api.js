// L'unico file che sa dove abita il backend e come si chiamano gli endpoint.
const API = 'http://localhost:8080/api'

// Nella versione TSX questo file dichiarava sei tipi. Senza TypeScript non
// esistono: restano qui sotto come commento, perche' le forme dei dati vanno
// comunque conosciute per scrivere il resto dell'applicazione.
//
//   type              'ORDER_SHIPPED' | 'ORDER_CANCELLED' | 'MESSAGE'
//
//   NotificationDto   { id, recipient, type, resourceId, title, createdAt, read }
//                     ricalca il record NotificationDto del backend. Se cambia il
//                     Java, va cambiato anche qui a mano: fra i due mondi non c'e'
//                     niente di automatico. createdAt e' ISO in UTC, per esempio
//                     "2025-03-14T10:22:31Z".
//
//   NotificaDiCanale  una NotificationDto in cui id puo' essere null.
//                     Sul canale WebSocket puo' arrivare anche un messaggio scritto
//                     da un altro utente con {"azione":"pubblica"}. Quel messaggio non
//                     e' salvato in nessuna tabella, quindi non ha un id, e l'id a null
//                     e' l'unico modo che abbiamo per distinguere "notifica vera, che
//                     ritrovi nello storico" da "messaggio di passaggio, che se non
//                     c'eri hai perso".
//
//   Busta             { canale, notifica }
//                     quello che arriva dal canale WebSocket: la notifica, piu' un campo
//                     che dice da quale "canale" arriva ("personale", "tutti", "ordine:42").
//                     Quel campo NON e' un'intestazione del protocollo: e' un dato che il
//                     backend si mette dentro il contenuto, perche' una WebSocket nuda
//                     consegna una stringa e nient'altro. Nella Dem 4 sparisce, perche'
//                     la destinazione la porta STOMP.
//
//   Pagina            { content, number, size, totalElements, totalPages, first, last }
//                     la Page di Spring Data, ridotta ai campi che l'interfaccia usa davvero.
//                     In TSX era generica, Pagina<T>, per dire anche di che cosa era la
//                     pagina; qui il contenuto e' semplicemente quello che arriva.
//
//   NuovaNotifica     { recipient, type, resourceId, title }
//                     il corpo che spediamo a /demo/notify.

// Valore iniziale, per non dover gestire "pagina ancora non caricata" con dei null
// sparsi in mezzo ai componenti.
export const PAGINA_VUOTA = {
  content: [],
  number: 0,
  size: 0,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
}

// ---------------------------------------------------------------------------
// Tre funzioncine di servizio, per non ripetere lo stesso controllo sei volte.
//
// Ricordate il tranello: fetch() NON considera un errore una risposta 404 o 500.
// La promise va a buon fine lo stesso, e ci consegna una risposta che dice "errore".
// Il controllo su risposta.ok tocca a noi.
//
// In TSX queste tre funzioni erano generiche (leggi<T>, inviaJson<T>): chi chiamava
// diceva che cosa si aspettava di ricevere. Qui restituiscono quello che arriva, e
// che cosa sia lo sa solo chi legge il commento sopra la chiamata.
// ---------------------------------------------------------------------------

async function leggi(percorso, cosa) {
  const risposta = await fetch(`${API}${percorso}`)
  if (!risposta.ok) throw new Error(`${cosa}: HTTP ${risposta.status}`)
  return risposta.json()
}

// Per POST e PATCH senza corpo: gli endpoint rispondono 204 No Content,
// quindi non c'e' niente da leggere, solo da controllare.
async function invia(percorso, metodo, cosa) {
  const risposta = await fetch(`${API}${percorso}`, { method: metodo })
  if (!risposta.ok) throw new Error(`${cosa}: HTTP ${risposta.status}`)
}

// Per le POST con un corpo JSON. L'header Content-Type e' obbligatorio: senza,
// Spring non sa come leggere il corpo e risponde 415 Unsupported Media Type.
async function inviaJson(percorso, corpo, cosa) {
  const risposta = await fetch(`${API}${percorso}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(corpo),
  })
  if (!risposta.ok) throw new Error(`${cosa}: HTTP ${risposta.status}`)
  return risposta.json()
}

// --- Le chiamate REST dell'applicazione ------------------------------------

// page e size finiscono nel Pageable del controller: a tagliare le pagine e'
// il database, non il browser. Caricare tutto e poi mostrarne cinque sarebbe
// sprecato con cento notifiche e impossibile con centomila.
// Restituisce una Pagina di NotificationDto.
export function caricaPagina(utente, page, size) {
  return leggi(
    `/notifications?recipient=${utente}&page=${page}&size=${size}&sort=createdAt,desc`,
    'storico',
  )
}

export async function contaNonLette(utente) {
  const corpo = await leggi(`/notifications/unread-count?recipient=${utente}`, 'conteggio')
  return corpo.count
}

export function segnaTutteLette(utente) {
  return invia(`/notifications/read-all?recipient=${utente}`, 'POST', 'read-all')
}

export function segnaLetta(id) {
  return invia(`/notifications/${id}/read`, 'PATCH', 'read')
}

/**
 * Il "telecomando" della lezione: crea una notifica per il destinatario indicato.
 *
 * Notate che questa e' una normale chiamata HTTP: non tocca il WebSocket.
 * E' il backend che, dopo aver salvato, spedisce la notifica sul canale.
 * Il browser che preme il pulsante non spedisce niente a nessuno.
 *
 * Il corpo e' una NuovaNotifica, la risposta una NotificationDto.
 */
export function inviaNotifica(corpo) {
  return inviaJson('/demo/notify', corpo, 'invio')
}
