export const CATEGORIES = ['BUCA', 'ILLUMINAZIONE', 'RIFIUTI', 'ALTRO'] as const

export type Category = (typeof CATEGORIES)[number]

/** Colore del marker per categoria: unica fonte di verita', usata anche dalla legenda. */
export const CATEGORY_COLOR: Record<Category, string> = {
  BUCA: '#d93025',
  ILLUMINAZIONE: '#f9ab00',
  RIFIUTI: '#1e8e3e',
  ALTRO: '#5f6368',
}

export const CATEGORY_LABEL: Record<Category, string> = {
  BUCA: 'Buca',
  ILLUMINAZIONE: 'Illuminazione',
  RIFIUTI: 'Rifiuti',
  ALTRO: 'Altro',
}

export interface Report {
  id: number
  category: Category
  description: string
  latitude: number
  longitude: number
  address: string | null
  createdAt: string
}

export interface Viewport {
  swLat: number
  swLng: number
  neLat: number
  neLng: number
}

export interface CreateReportPayload {
  category: Category
  description: string
  latitude: number
  longitude: number
  address?: string | null
}

export interface GeocodeResult {
  latitude: number
  longitude: number
  formattedAddress: string
}
