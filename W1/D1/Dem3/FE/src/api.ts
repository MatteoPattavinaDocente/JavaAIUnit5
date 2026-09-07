const BASE_URL = 'http://localhost:8080'

// O le coordinate, o l'indirizzo: il backend rifiuta chi manda entrambi o nessuno.
export type CreatePostRequest = {
  title: string
  latitude?: number
  longitude?: number
  address?: string
}

// Il tipo dice number | string perche' un BigDecimal puo' arrivare in tutti e due i modi:
// con la configurazione di default Jackson lo serializza come numero (44.102534), ma
// basta un write-numbers-as-strings, o un backend che quota i decimali per non perdere
// precisione in JavaScript, e diventa "44.102534". Number() copre entrambi i casi.
// Passare una stringa ad AdvancedMarker non da' errore: il marker semplicemente sparisce.
export type PostResponse = {
  id: number
  title: string
  latitude: number | string | null
  longitude: number | string | null
  formattedAddress: string | null
  createdAt: string
}

export type Bounds = {
  north: number
  south: number
  east: number
  west: number
}

type ApiError = {
  timestamp: string
  status: number
  error: string
  messages: string[]
}

async function leggiRisposta<T>(res: Response): Promise<T> {
  // fetch non lancia sugli errori HTTP: un 404 e' una risposta riuscita.
  // Il controllo su res.ok e' nostro.
  if (!res.ok) {
    const err: ApiError = await res.json()
    throw new Error(err.messages.join(' — '))
  }
  return res.json()
}

export async function createPost(body: CreatePostRequest): Promise<PostResponse> {
  const res = await fetch(`${BASE_URL}/api/posts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  return leggiRisposta<PostResponse>(res)
}

export async function fetchPosts(bounds: Bounds): Promise<PostResponse[]> {
  const query = new URLSearchParams({
    north: String(bounds.north),
    south: String(bounds.south),
    east: String(bounds.east),
    west: String(bounds.west),
  })
  const res = await fetch(`${BASE_URL}/api/posts?${query}`)
  return leggiRisposta<PostResponse[]>(res)
}
