// L'unico file che sa dove abita il backend e come si chiamano gli endpoint.
const API = 'http://localhost:8080/api'

export type NotificationType = 'ORDER_SHIPPED' | 'ORDER_CANCELLED' | 'MESSAGE'

// Ricalca il record NotificationDto del backend. Se cambia il Java, va cambiato
// anche qui a mano: fra i due mondi non c'e' niente di automatico.
export type NotificationDto = {
  id: number
  recipient: string
  type: NotificationType
  resourceId: number | null
  title: string
  createdAt: string // ISO in UTC, per esempio "2025-03-14T10:22:31Z"
  read: boolean
}

/**
 * Sul canale WebSocket puo' arrivare anche un messaggio scritto da un altro utente
 * con {"azione":"pubblica"}. Quel messaggio non e' salvato in nessuna tabella,
 * quindi non ha un id.
 *
 * L'id a null e' l'unico modo che abbiamo per distinguere "notifica vera, che
 * ritrovi nello storico" da "messaggio di passaggio, che se non c'eri hai perso".
 */
export type NotificaDiCanale = Omit<NotificationDto, 'id'> & { id: number | null }

/**
 * Quello che arriva dal canale WebSocket: la notifica, piu' un campo che dice
 * da quale "canale" arriva ("personale", "tutti", "ordine:42").
 *
 * Quel campo NON e' un'intestazione del protocollo: e' un dato che il backend
 * si mette dentro il contenuto, perche' una WebSocket nuda consegna una stringa
 * e nient'altro. Nella Dem 4 sparisce, perche' la destinazione la porta STOMP.
 */
export type Busta = {
  canale: string
  notifica: NotificaDiCanale
}

/** La Page di Spring Data, ridotta ai campi che l'interfaccia usa davvero. */
export type Pagina<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

// Valore iniziale, per non dover gestire "pagina ancora non caricata" con dei null
// sparsi in mezzo ai componenti.
export const PAGINA_VUOTA: Pagina<NotificationDto> = {
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
// ---------------------------------------------------------------------------

async function leggi<T>(percorso: string, cosa: string): Promise<T> {
  const risposta = await fetch(`${API}${percorso}`)
  if (!risposta.ok) throw new Error(`${cosa}: HTTP ${risposta.status}`)
  return risposta.json() as Promise<T>
}

// Per POST e PATCH senza corpo: gli endpoint rispondono 204 No Content,
// quindi non c'e' niente da leggere, solo da controllare.
async function invia(percorso: string, metodo: 'POST' | 'PATCH', cosa: string): Promise<void> {
  const risposta = await fetch(`${API}${percorso}`, { method: metodo })
  if (!risposta.ok) throw new Error(`${cosa}: HTTP ${risposta.status}`)
}

// Per le POST con un corpo JSON. L'header Content-Type e' obbligatorio: senza,
// Spring non sa come leggere il corpo e risponde 415 Unsupported Media Type.
async function inviaJson<T>(percorso: string, corpo: unknown, cosa: string): Promise<T> {
  const risposta = await fetch(`${API}${percorso}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(corpo),
  })
  if (!risposta.ok) throw new Error(`${cosa}: HTTP ${risposta.status}`)
  return risposta.json() as Promise<T>
}

// --- Le chiamate REST dell'applicazione ------------------------------------

// page e size finiscono nel Pageable del controller: a tagliare le pagine e'
// il database, non il browser. Caricare tutto e poi mostrarne cinque sarebbe
// sprecato con cento notifiche e impossibile con centomila.
export function caricaPagina(
  utente: string,
  page: number,
  size: number,
): Promise<Pagina<NotificationDto>> {
  return leggi(
    `/notifications?recipient=${utente}&page=${page}&size=${size}&sort=createdAt,desc`,
    'storico',
  )
}

export async function contaNonLette(utente: string): Promise<number> {
  const corpo = await leggi<{ count: number }>(
    `/notifications/unread-count?recipient=${utente}`,
    'conteggio',
  )
  return corpo.count
}

export function segnaTutteLette(utente: string): Promise<void> {
  return invia(`/notifications/read-all?recipient=${utente}`, 'POST', 'read-all')
}

export function segnaLetta(id: number): Promise<void> {
  return invia(`/notifications/${id}/read`, 'PATCH', 'read')
}

export type NuovaNotifica = {
  recipient: string
  type: NotificationType
  resourceId: number | null
  title: string
}

/**
 * Il "telecomando" della lezione: crea una notifica per il destinatario indicato.
 *
 * Notate che questa e' una normale chiamata HTTP: non tocca il WebSocket.
 * E' il backend che, dopo aver salvato, spedisce la notifica sul canale.
 * Il browser che preme il pulsante non spedisce niente a nessuno.
 */
export function inviaNotifica(corpo: NuovaNotifica): Promise<NotificationDto> {
  return inviaJson<NotificationDto>('/demo/notify', corpo, 'invio')
}
