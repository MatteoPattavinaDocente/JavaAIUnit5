const API = 'http://localhost:8080/api'

export type MessaggioRisposta = {
  id: number | null
  conversazione: number | null
  mittente: string
  destinatario: string
  testo: string
  istante: string
  stato: string
  idTemporaneo: string | null
}

export type ConversazioneRiepilogo = {
  id: number
  controparte: string
  nonLetti: number
  ultimoTesto: string | null
  ultimoIstante: string | null
  controparteCollegata: boolean
}

export type PaginaMessaggi = {
  // null finché i due non si sono mai scritti: la riga nasce con il primo
  // messaggio, non con l'apertura della chat.
  conversazione: number | null
  messaggi: MessaggioRisposta[]
  ultimoId: number | null
  nonLetti: number
}

export type Aggiornamento = {
  tipo: 'LETTI' | 'SCRIVE'
  conversazione: number
  utente: string
  messaggi: number[]
}

export async function login(utente: string): Promise<{ utente: string; token: string }> {
  const res = await fetch(`${API}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ utente }),
  })
  if (!res.ok) throw new Error('login fallito')
  return res.json()
}

export async function conversazioni(utente: string): Promise<ConversazioneRiepilogo[]> {
  const res = await fetch(`${API}/conversazioni?utente=${encodeURIComponent(utente)}`)
  if (!res.ok) throw new Error(`conversazioni: HTTP ${res.status}`)
  return res.json()
}

/** L'ultima pagina: letta una volta, all'apertura della conversazione. */
export async function cronologia(utente: string, con: string): Promise<PaginaMessaggi> {
  const res = await fetch(
    `${API}/messaggi?utente=${encodeURIComponent(utente)}&con=${encodeURIComponent(con)}`,
  )
  // Senza questo controllo il corpo di un errore 500 viene preso per una
  // pagina valida: messaggi resta undefined e la lista esplode al primo
  // ridisegno, lontano dalla vera causa.
  if (!res.ok) throw new Error(`cronologia: HTTP ${res.status}`)
  return res.json()
}

/** Il buco temporale: solo quello arrivato dopo l'ultimo messaggio che abbiamo. */
export async function messaggiDopo(utente: string, con: string, dopo: number): Promise<PaginaMessaggi> {
  const res = await fetch(
    `${API}/messaggi/dopo?utente=${encodeURIComponent(utente)}&con=${encodeURIComponent(con)}&dopo=${dopo}`,
  )
  if (!res.ok) throw new Error(`messaggiDopo: HTTP ${res.status}`)
  return res.json()
}

export async function riempi(utente: string, con: string, quanti = 120): Promise<void> {
  await fetch(
    `${API}/demo/riempi?utente=${encodeURIComponent(utente)}&con=${encodeURIComponent(con)}&quanti=${quanti}`,
    { method: 'POST' },
  )
}

export async function azzeraTutto(): Promise<void> {
  await fetch(`${API}/demo/tutto`, { method: 'DELETE' })
}
