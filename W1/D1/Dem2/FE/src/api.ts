export type CreatePostRequest = {
  title: string
  // Numeri, non stringhe: nel JSON viaggiano come 44.102534, sempre con il punto
  // decimale. La virgola italiana non e' JSON valido.
  latitude: number
  longitude: number
}

export type PostResponse = {
  id: number
  title: string
  latitude: number
  longitude: number
  createdAt: string
}

type ApiError = {
  timestamp: string
  status: number
  error: string
  messages: string[]
}

export async function createPost(body: CreatePostRequest): Promise<PostResponse> {
  const res = await fetch('http://localhost:8080/api/posts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  // fetch non lancia sugli errori HTTP: un 400 e' una risposta riuscita.
  // Il controllo su res.ok e' nostro, altrimenti il 400 passerebbe per un successo.
  if (!res.ok) {
    const err: ApiError = await res.json()
    throw new Error(err.messages.join(' — '))
  }

  return res.json()
}
