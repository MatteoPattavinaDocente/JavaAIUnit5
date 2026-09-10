export type TipoNotifica = 'PERSONAL' | 'CANALE' | 'ALL'

export interface AuthResponse {
  id: string
  username: string
  token: string
}

export interface Canale {
  id: string
  nome: string
  descrizione: string | null
  /** Proprietario del canale: serve a distinguere i canali propri da quelli altrui. */
  idUtente: string
}

export interface Notifica {
  id: string
  tipo: TipoNotifica
  idCanale: string | null
  message: string
  createdAt: string
  readAt: string | null
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface EsitoCreazioneNotifica {
  tipo: TipoNotifica
  destinatari: number
  inviateViaWebSocket: number
}

/** Corpo di errore restituito dal GlobalExceptionHandler del backend. */
export interface ErroreApi {
  status: number
  errore: string
  messaggio: string
  dettagli: string[]
  timestamp: string
}
