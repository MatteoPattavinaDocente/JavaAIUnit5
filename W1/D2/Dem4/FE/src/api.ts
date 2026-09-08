// L'unico file che sa dove abita il backend.
const API = 'http://localhost:8080/api'

export type NotificationType = 'ORDER_SHIPPED' | 'ORDER_CANCELLED' | 'MESSAGE'

// Ricalca il record NotificationDto del backend.
export type NotificationDto = {
  id: number
  recipient: string
  type: NotificationType
  resourceId: number | null
  title: string
  createdAt: string // ISO in UTC
  read: boolean
}

type Pagina = { content: NotificationDto[] }

/**
 * IL CONTATORE DELLE RICHIESTE HTTP.
 *
 * E' l'altra meta' del confronto della Dem 4. Guardatelo mentre provate:
 *  - con STOMP questo numero sta fermo, e intanto le notifiche continuano ad arrivare;
 *  - col polling cresce da solo ogni 5 secondi, anche quando non succede niente.
 *
 * E' una variabile del modulo, non uno stato React: cresce anche quando nessun
 * componente sta guardando. I componenti la leggono con richiesteHttp() quando serve.
 */
let richieste = 0
export const richiesteHttp = () => richieste

/**
 * Ogni chiamata HTTP passa da qui, cosi' il contatore non puo' sfuggire.
 *
 * Ricordate: fetch() non considera un errore una risposta 404 o 500,
 * il controllo su res.ok tocca a noi.
 */
async function chiedi(url: string, init?: RequestInit): Promise<Response> {
  richieste++
  const res = await fetch(url, init)
  if (!res.ok) throw new Error(`${url}: HTTP ${res.status}`)
  return res
}

// --- Lettura: lo storico e il conteggio, cioe' la verita' sul database ------

export async function caricaStorico(utente: string): Promise<NotificationDto[]> {
  const res = await chiedi(`${API}/notifications?recipient=${utente}&size=20`)
  const pagina: Pagina = await res.json()
  return pagina.content
}

export async function contaNonLette(utente: string): Promise<number> {
  const res = await chiedi(`${API}/notifications/unread-count?recipient=${utente}`)
  const corpo: { count: number } = await res.json()
  return corpo.count
}

export async function segnaTutteLette(utente: string): Promise<void> {
  await chiedi(`${API}/notifications/read-all?recipient=${utente}`, { method: 'POST' })
}

// --- I due invii che passano da HTTP ---------------------------------------
//
// L'andata e' identica: due POST uguali, con gli stessi parametri.
// La differenza sta tutta nel backend: il primo salva e basta, il secondo
// annuncia l'evento e il broker consegna. Il terzo modo di mandare un messaggio
// (via frame STOMP) non passa di qui: sta in useNotifiche, perche' non usa HTTP.

export async function inviaSenzaPush(da: string, a: string, testo: string): Promise<void> {
  const q = `from=${encodeURIComponent(da)}&to=${encodeURIComponent(a)}&text=${encodeURIComponent(testo)}`
  await chiedi(`${API}/demo/messaggi/senza-push?${q}`, { method: 'POST' })
}

export async function inviaConPush(da: string, a: string, testo: string): Promise<void> {
  const q = `from=${encodeURIComponent(da)}&to=${encodeURIComponent(a)}&text=${encodeURIComponent(testo)}`
  await chiedi(`${API}/demo/messaggi/con-push?${q}`, { method: 'POST' })
}
