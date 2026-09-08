// L'unico file che sa dove abita il backend. Se domani l'indirizzo cambia,
// si cambia qui e basta: nessun altro file scrive URL a mano.
const API = 'http://localhost:8080/api'

// Nella versione TSX qui c'erano tre tipi esportati. In JavaScript non esistono,
// quindi la forma dei dati resta scritta soltanto in questo commento e dobbiamo
// ricordarcela noi:
//
//   type            'ORDER_SHIPPED' | 'ORDER_CANCELLED' | 'MESSAGE'
//
//   NotificationDto { id, type, resourceId, title, createdAt, read }
//                   ricalca il record NotificationDto del backend. createdAt e' una
//                   data in formato ISO, per esempio "2025-03-14T10:22:31Z".
//
//   Pagina          { content, number, totalPages, totalElements }
//                   la forma di una pagina di Spring Data: il contenuto piu' le
//                   informazioni per costruire i pulsanti "precedenti / successive".
//                   number e' il numero di questa pagina, partendo da 0.

// ---------------------------------------------------------------------------
// Due funzioncine di servizio, per non ripetere lo stesso controllo cinque volte.
//
// Attenzione a un dettaglio che sorprende sempre: fetch() NON considera un errore
// una risposta 404 o 500. La promise va a buon fine lo stesso, e ci consegna una
// risposta che dice "errore". Il controllo su risposta.ok dobbiamo farlo noi.
// ---------------------------------------------------------------------------

// Per le GET: chiede, controlla, restituisce il JSON.
// Cosa contenga quel JSON qui non e' scritto da nessuna parte: lo sa solo chi chiama.
async function leggi(percorso) {
  const risposta = await fetch(`${API}${percorso}`)
  if (!risposta.ok) throw new Error(`Il backend ha risposto ${risposta.status}`)
  return risposta.json()
}

// Per le PATCH e le POST: manda il comando e controlla che sia andato bene.
// Non legge il corpo perche' questi endpoint rispondono 204 No Content: non c'e' niente da leggere.
async function invia(percorso, metodo) {
  const risposta = await fetch(`${API}${percorso}`, { method: metodo })
  if (!risposta.ok) throw new Error(`Il backend ha risposto ${risposta.status}`)
}

// --- Le cinque chiamate che l'applicazione sa fare -------------------------

// Restituisce una Pagina di NotificationDto.
export function caricaPagina(utente, pagina) {
  return leggi(`/notifications?recipient=${utente}&page=${pagina}&size=5`)
}

export async function contaNonLette(utente) {
  // Il backend risponde { "count": 3 }: a noi serve solo il numero.
  const corpo = await leggi(`/notifications/unread-count?recipient=${utente}`)
  return corpo.count
}

export function segnaLetta(id) {
  return invia(`/notifications/${id}/read`, 'PATCH')
}

export function segnaTutteLette(utente) {
  return invia(`/notifications/read-all?recipient=${utente}`, 'POST')
}

// Il "telecomando" della lezione: fa nascere una notifica senza un ordine vero dietro.
export function spedisciOrdine(ordineId, utente) {
  return invia(`/demo/orders/${ordineId}/ship?recipient=${utente}`, 'POST')
}
