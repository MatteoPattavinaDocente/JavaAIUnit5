import type {
  AuthResponse,
  Canale,
  EsitoCreazioneNotifica,
  ErroreApi,
  Notifica,
  PageResponse,
  TipoNotifica,
} from './types'

export const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

/** Errore applicativo con lo status HTTP, per distinguere 401 e 400 nei componenti. */
export class ApiError extends Error {
  readonly status: number
  readonly dettagli: string[]

  constructor(status: number, messaggio: string, dettagli: string[] = []) {
    super(messaggio)
    this.status = status
    this.dettagli = dettagli
  }
}

let tokenCorrente: string | null = null
/** Invocata quando il backend risponde 401: serve a forzare il logout nella UI. */
let onNonAutorizzato: (() => void) | null = null

export function impostaToken(token: string | null) {
  tokenCorrente = token
}

export function impostaHandlerNonAutorizzato(handler: (() => void) | null) {
  onNonAutorizzato = handler
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (tokenCorrente) headers.Authorization = `Bearer ${tokenCorrente}`

  const res = await fetch(API_BASE + path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (res.status === 204) return undefined as T

  const testo = await res.text()
  const dati = testo ? JSON.parse(testo) : null

  if (!res.ok) {
    const errore = dati as ErroreApi | null
    // il token non e' piu' valido: la sessione va chiusa ovunque nella UI
    if (res.status === 401 && tokenCorrente) onNonAutorizzato?.()
    throw new ApiError(res.status, errore?.messaggio ?? 'Errore imprevisto', errore?.dettagli ?? [])
  }

  return dati as T
}

export const api = {
  register: (username: string, password: string) =>
    request<AuthResponse>('POST', '/api/auth/register', { username, password }),

  login: (username: string, password: string) =>
    request<AuthResponse>('POST', '/api/auth/login', { username, password }),

  logout: () => request<void>('POST', '/api/auth/logout'),

  canali: (page = 0) => request<PageResponse<Canale>>('GET', `/api/canali?page=${page}`),

  canale: (idCanale: string) => request<Canale>('GET', `/api/canali/${idCanale}`),

  canaliIscritto: (page = 0) =>
    request<PageResponse<Canale>>('GET', `/api/canali/iscritto?page=${page}`),

  creaCanale: (nome: string, descrizione: string) =>
    request<Canale>('POST', '/api/canali', { nome, descrizione }),

  follow: (idCanale: string) => request<void>('POST', '/api/iscrizioni', { idCanale }),

  unfollow: (idCanale: string) => request<void>('DELETE', `/api/iscrizioni/${idCanale}`),

  notifiche: (page = 0) => request<PageResponse<Notifica>>('GET', `/api/notifiche?page=${page}`),

  contaNonLette: () => request<{ nonLette: number }>('GET', '/api/notifiche/count'),

  marcaLetta: (idNotifica: string) =>
    request<Notifica>('PATCH', `/api/notifiche/${idNotifica}/read`),

  marcaTutteLette: () => request<{ nonLette: number }>('POST', '/api/notifiche/read-all'),

  creaNotifica: (payload: {
    tipo: TipoNotifica
    message: string
    idCanale?: string | null
    idDestinatario?: string | null
  }) => request<EsitoCreazioneNotifica>('POST', '/api/notifiche', payload),
}
