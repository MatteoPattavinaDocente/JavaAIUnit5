const API = 'http://localhost:8080/api'

export type MessaggioRisposta = {
  id: number
  mittente: string
  destinatario: string
  testo: string
  istante: string
  stato: string
  idTemporaneo: string | null
}

export type Evento = {
  istante: string
  tipo: 'APERTA' | 'CHIUSA'
  utente: string
  aperteDopo: number
}

export type Sessioni = { aperte: number; storico: Evento[] }

export async function login(utente: string): Promise<{ utente: string; token: string }> {
  const res = await fetch(`${API}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ utente }),
  })
  if (!res.ok) throw new Error('login fallito')
  return res.json()
}

export async function cronologia(utente: string, con: string): Promise<MessaggioRisposta[]> {
  const res = await fetch(`${API}/messaggi?utente=${encodeURIComponent(utente)}&con=${encodeURIComponent(con)}`)
  return res.json()
}

export async function leggiSessioni(): Promise<Sessioni> {
  const res = await fetch(`${API}/sessioni`)
  return res.json()
}

export async function azzeraStorico(): Promise<void> {
  await fetch(`${API}/sessioni`, { method: 'DELETE' })
}
