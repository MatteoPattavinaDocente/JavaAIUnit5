// I typedef delle forme dei dati stanno in constants.js (era types.ts nella versione TSX).

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'

export class ApiError extends Error {
  constructor(status, message, details = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

async function parse(response) {
  if (response.ok) {
    // In TypeScript qui c'era un "as T": una promessa sul contenuto del JSON, che
    // nessuno verificava a runtime. Togliendola non perdiamo nessun controllo reale.
    return await response.json()
  }
  const body = await response.json().catch(() => null)
  throw new ApiError(
    response.status,
    body?.message ?? `Errore ${response.status}`,
    body?.details ?? [],
  )
}

/**
 * Solo le segnalazioni dentro il riquadro visibile; il filtro categoria lo applica il server.
 * @param {import('./constants').Viewport} viewport
 * @param {import('./constants').Category|null} category
 * @param {AbortSignal} [signal]
 * @returns {Promise<import('./constants').Report[]>}
 */
export async function fetchReports(viewport, category, signal) {
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
  return parse(response)
}

/**
 * @param {import('./constants').CreateReportPayload} payload
 * @returns {Promise<import('./constants').Report>}
 */
export async function createReport(payload) {
  const response = await fetch(`${BASE_URL}/reports`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  return parse(response)
}

/**
 * Il geocoding passa dal backend: la chiave di server non e' nel bundle.
 * @param {string} address
 * @param {AbortSignal} [signal]
 * @returns {Promise<import('./constants').GeocodeResult>}
 */
export async function geocodeAddress(address, signal) {
  const response = await fetch(`${BASE_URL}/geocode?address=${encodeURIComponent(address)}`, { signal })
  return parse(response)
}
