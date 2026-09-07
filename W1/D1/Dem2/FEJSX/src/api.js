// Versione JSX: senza TypeScript i tipi delle richieste e delle risposte non
// esistono a compile time. Li documentiamo con JSDoc, cosi' l'editor continua a
// suggerire i campi, ma nessuno li verifica: un titolo scritto "titel" parte
// comunque e il backend risponde 400.

/**
 * @typedef {object} CreatePostRequest
 * @property {string} title
 * @property {number} latitude  Numero, non stringa: nel JSON viaggia come
 *   44.102534, sempre con il punto decimale. La virgola italiana non e' JSON valido.
 * @property {number} longitude
 */

/**
 * @typedef {object} PostResponse
 * @property {number} id
 * @property {string} title
 * @property {number} latitude
 * @property {number} longitude
 * @property {string} createdAt
 */

/** @param {CreatePostRequest} body @returns {Promise<PostResponse>} */
export async function createPost(body) {
  const res = await fetch('http://localhost:8080/api/posts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  // fetch non lancia sugli errori HTTP: un 400 e' una risposta riuscita.
  // Il controllo su res.ok e' nostro, altrimenti il 400 passerebbe per un successo.
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.messages.join(' — '))
  }

  return res.json()
}
