// L'unico file che sa dove abita il backend. Se domani l'indirizzo cambia,
// si cambia qui e basta: nessun altro file scrive URL a mano.
const API = 'http://localhost:8080/api'

export type NotificationType = 'ORDER_SHIPPED' | 'ORDER_CANCELLED' | 'MESSAGE'

// Questi tipi ricalcano il record NotificationDto del backend.
// Non c'e' niente di automatico: se cambia il Java, va cambiato anche qui a mano.
export type NotificationDto = {
  id: number
  type: NotificationType
  resourceId: number | null
  title: string
  createdAt: string // data in formato ISO, per esempio "2025-03-14T10:22:31Z"
  read: boolean
}

// La forma di una pagina di Spring Data: il contenuto piu' le informazioni
// per costruire i pulsanti "precedenti / successive".
export type Pagina = {
  content: NotificationDto[]
  number: number // numero di questa pagina, partendo da 0
  totalPages: number
  totalElements: number
}

// ---------------------------------------------------------------------------
// Due funzioncine di servizio, per non ripetere lo stesso controllo cinque volte.
//
// Attenzione a un dettaglio che sorprende sempre: fetch() NON considera un errore
// una risposta 404 o 500. La promise va a buon fine lo stesso, e ci consegna una
// risposta che dice "errore". Il controllo su risposta.ok dobbiamo farlo noi.
// ---------------------------------------------------------------------------

// Per le GET: chiede, controlla, restituisce il JSON gia' tipizzato.
async function leggi<T>(percorso: string): Promise<T> {
  const risposta = await fetch(`${API}${percorso}`)
  if (!risposta.ok) throw new Error(`Il backend ha risposto ${risposta.status}`)
  return risposta.json() as Promise<T>
}

// Per le PATCH e le POST: manda il comando e controlla che sia andato bene.
// Non legge il corpo perche' questi endpoint rispondono 204 No Content: non c'e' niente da leggere.
async function invia(percorso: string, metodo: 'POST' | 'PATCH'): Promise<void> {
  const risposta = await fetch(`${API}${percorso}`, { method: metodo })
  if (!risposta.ok) throw new Error(`Il backend ha risposto ${risposta.status}`)
}

// --- Le cinque chiamate che l'applicazione sa fare -------------------------

export function caricaPagina(utente: string, pagina: number): Promise<Pagina> {
  return leggi<Pagina>(`/notifications?recipient=${utente}&page=${pagina}&size=5`)
}

export async function contaNonLette(utente: string): Promise<number> {
  // Il backend risponde { "count": 3 }: a noi serve solo il numero.
  const corpo = await leggi<{ count: number }>(`/notifications/unread-count?recipient=${utente}`)
  return corpo.count
}

export function segnaLetta(id: number): Promise<void> {
  return invia(`/notifications/${id}/read`, 'PATCH')
}

export function segnaTutteLette(utente: string): Promise<void> {
  return invia(`/notifications/read-all?recipient=${utente}`, 'POST')
}

// Il "telecomando" della lezione: fa nascere una notifica senza un ordine vero dietro.
export function spedisciOrdine(ordineId: number, utente: string): Promise<void> {
  return invia(`/demo/orders/${ordineId}/ship?recipient=${utente}`, 'POST')
}
