import { useState } from 'react'
import { createPost } from './api'

function PostForm({ posizione }) {
  const [titolo, setTitolo] = useState('')
  const [esito, setEsito] = useState(null)
  const [invioInCorso, setInvioInCorso] = useState(false)

  async function handleSubmit(e) {
    // Senza questo il browser ricarica la pagina e la richiesta non parte mai.
    e.preventDefault()
    setInvioInCorso(true)
    setEsito(null)

    try {
      const salvato = await createPost({
        title: titolo,
        latitude: posizione.lat,
        longitude: posizione.lng,
      })
      setEsito(`Salvato con id ${salvato.id}`)
      setTitolo('')
    } catch (err) {
      // instanceof Error resta necessario anche in JavaScript: in un catch puo'
      // arrivare qualunque cosa, non solo un Error.
      setEsito(err instanceof Error ? err.message : 'Errore imprevisto')
    } finally {
      setInvioInCorso(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <label>
        Titolo{' '}
        <input value={titolo} onChange={(e) => setTitolo(e.target.value)} />
      </label>
      <button type="submit" disabled={invioInCorso || !titolo.trim()}>
        {invioInCorso ? 'Salvataggio…' : 'Salva'}
      </button>
      {esito && <p>{esito}</p>}
    </form>
  )
}

export default PostForm
