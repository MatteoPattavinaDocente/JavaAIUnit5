const API = 'http://localhost:8080/api'

export type MessaggioRisposta = {
  id: number | null
  mittente: string
  destinatario: string
  testo: string
  istante: string
  stato: string
  idTemporaneo: string | null
}

export type Presenza = {
  utenti: number
  sessioni: number
  collegati: { nome: string; sessioni: number }[]
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

/** La cronologia arriva da REST, non dal canale: il canale non ripete il passato. */
export async function cronologia(utente: string, con: string): Promise<MessaggioRisposta[]> {
  const res = await fetch(`${API}/messaggi?utente=${encodeURIComponent(utente)}&con=${encodeURIComponent(con)}`)
  return res.json()
}

export async function leggiPresenza(): Promise<Presenza> {
  const res = await fetch(`${API}/presenza`)
  return res.json()
}

export async function azzeraArchivio(): Promise<void> {
  await fetch(`${API}/messaggi`, { method: 'DELETE' })
}
