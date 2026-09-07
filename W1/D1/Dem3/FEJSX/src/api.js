const BASE_URL = 'http://localhost:8080'

// Versione JSX: i type di TypeScript diventano typedef JSDoc. Servono a chi legge
// e ai suggerimenti dell'editor, non al browser: nessun controllo a compile time.

/**
 * O le coordinate, o l'indirizzo: il backend rifiuta chi manda entrambi o nessuno.
 * @typedef {object} CreatePostRequest
 * @property {string} title
 * @property {number} [latitude]
 * @property {number} [longitude]
 * @property {string} [address]
 */

/**
 * latitude e longitude possono arrivare come numero O come stringa: un BigDecimal
 * puo' viaggiare in tutti e due i modi. Con la configurazione di default Jackson lo
 * serializza come numero (44.102534), ma basta un write-numbers-as-strings, o un
 * backend che quota i decimali per non perdere precisione in JavaScript, e diventa
 * "44.102534". Number() copre entrambi i casi.
 * Passare una stringa ad AdvancedMarker non da' errore: il marker semplicemente sparisce.
 * Qui il rischio e' piu' alto che nella versione TypeScript, dove il tipo
 * number | string obbligava a pensarci: in JavaScript nessuno ce lo ricorda.
 * @typedef {object} PostResponse
 * @property {number} id
 * @property {string} title
 * @property {number|string|null} latitude
 * @property {number|string|null} longitude
 * @property {string|null} formattedAddress
 * @property {string} createdAt
 */

/**
 * @typedef {object} Bounds
 * @property {number} north
 * @property {number} south
 * @property {number} east
 * @property {number} west
 */

async function leggiRisposta(res) {
  // fetch non lancia sugli errori HTTP: un 404 e' una risposta riuscita.
  // Il controllo su res.ok e' nostro.
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.messages.join(' — '))
  }
  return res.json()
}

/**
 * @param {CreatePostRequest} body
 * @returns {Promise<PostResponse>}
 */
export async function createPost(body) {
  const res = await fetch(`${BASE_URL}/api/posts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  return leggiRisposta(res)
}

/**
 * @param {Bounds} bounds
 * @returns {Promise<PostResponse[]>}
 */
export async function fetchPosts(bounds) {
  const query = new URLSearchParams({
    north: String(bounds.north),
    south: String(bounds.south),
    east: String(bounds.east),
    west: String(bounds.west),
  })
  const res = await fetch(`${BASE_URL}/api/posts?${query}`)
  return leggiRisposta(res)
}
