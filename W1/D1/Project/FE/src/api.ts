import type { Category, CreateReportPayload, GeocodeResult, Report, Viewport } from './types'

const BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? '/api'

export class ApiError extends Error {
  readonly status: number
  readonly details: string[]

  constructor(status: number, message: string, details: string[] = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

async function parse<T>(response: Response): Promise<T> {
  if (response.ok) {
    return (await response.json()) as T
  }
  const body = await response.json().catch(() => null)
  throw new ApiError(
    response.status,
    body?.message ?? `Errore ${response.status}`,
    body?.details ?? [],
  )
}

/** Solo le segnalazioni dentro il riquadro visibile; il filtro categoria lo applica il server. */
export async function fetchReports(
  viewport: Viewport,
  category: Category | null,
  signal?: AbortSignal,
): Promise<Report[]> {
  const params = new URLSearchParams({
    swLat: String(viewport.swLat),
    swLng: String(viewport.swLng),
    neLat: String(viewport.neLat),
    neLng: String(viewport.neLng),
  })
  if (category) {
    params.set('category', category)
  }
  const response = await fetch(`${BASE_URL}/reports?${params}`, { signal })
  return parse<Report[]>(response)
}

export async function createReport(payload: CreateReportPayload): Promise<Report> {
  const response = await fetch(`${BASE_URL}/reports`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  return parse<Report>(response)
}

/** Il geocoding passa dal backend: la chiave di server non e' nel bundle. */
export async function geocodeAddress(address: string, signal?: AbortSignal): Promise<GeocodeResult> {
  const response = await fetch(`${BASE_URL}/geocode?address=${encodeURIComponent(address)}`, { signal })
  return parse<GeocodeResult>(response)
}
