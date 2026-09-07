import { useState } from 'react'
import { createPost } from './api'

type Props = {
  posizione: google.maps.LatLngLiteral | null
  onSalvato: () => void
}

function PostForm({ posizione, onSalvato }: Props) {
  const [titolo, setTitolo] = useState('')
  const [indirizzo, setIndirizzo] = useState('')
  const [esito, setEsito] = useState<string | null>(null)
  const [invioInCorso, setInvioInCorso] = useState(false)

  // Le due strade si escludono, come nel record del backend: se c'e' l'indirizzo si
  // geocodifica, altrimenti si usano le coordinate del click.
  const usaIndirizzo = indirizzo.trim().length > 0
  const puoSalvare = titolo.trim().length > 0 && (usaIndirizzo || posizione !== null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setInvioInCorso(true)
    setEsito(null)

    try {
      const salvato = await createPost(
        usaIndirizzo
          ? { title: titolo, address: indirizzo }
          : { title: titolo, latitude: posizione!.lat, longitude: posizione!.lng }
      )
      setEsito(`Salvato con id ${salvato.id}${salvato.formattedAddress ? ` — ${salvato.formattedAddress}` : ''}`)
      setTitolo('')
      setIndirizzo('')
      onSalvato()
    } catch (err) {
      setEsito(err instanceof Error ? err.message : 'Errore imprevisto')
    } finally {
      setInvioInCorso(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <label>
        Titolo <input value={titolo} onChange={(e) => setTitolo(e.target.value)} />
      </label>{' '}
      <label>
        Indirizzo{' '}
        <input
          value={indirizzo}
          onChange={(e) => setIndirizzo(e.target.value)}
          placeholder={posizione ? 'oppure clicca sulla mappa' : 'Via del Prione 1, La Spezia'}
        />
      </label>{' '}
      <button type="submit" disabled={invioInCorso || !puoSalvare}>
        {invioInCorso ? 'Salvataggio…' : 'Salva'}
      </button>
      {esito && <p>{esito}</p>}
    </form>
  )
}

export default PostForm
