const API = 'http://localhost:8080/api'

export type Presenza = {
  utenti: number
  sessioni: number
  collegati: { nome: string; sessioni: number }[]
}

export type FrameLog = {
  istante: string
  mittente: string
  destinazione: string
  testo: string
}

/** Nessuna password: il server consegna un token per il nome che gli si passa. */
export async function login(utente: string): Promise<{ utente: string; token: string }> {
  const res = await fetch(`${API}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ utente }),
  })
  if (!res.ok) throw new Error('login fallito')
  return res.json()
}

export async function leggiPresenza(): Promise<Presenza> {
  const res = await fetch(`${API}/presenza`)
  return res.json()
}

export async function leggiSessioni(): Promise<{ aperte: number }> {
  const res = await fetch(`${API}/sessioni`)
  return res.json()
}
