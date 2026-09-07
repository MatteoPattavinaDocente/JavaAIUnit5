import { useState } from 'react'
import LocationPicker from './LocationPicker'
import PostForm from './PostForm'

function App() {
  // Lo stato e' salito qui perche' ora lo leggono in due: la mappa e il form.
  const [posizione, setPosizione] = useState(null)

  return (
    <main>
      <h1>Scegli una posizione</h1>
      <LocationPicker posizione={posizione} onChange={setPosizione} />

      {/* La key rimonta il form quando il punto cambia: titolo ed esito ripartono
          puliti invece di restare appiccicati alla posizione precedente. */}
      {posizione && <PostForm key={`${posizione.lat},${posizione.lng}`} posizione={posizione} />}
    </main>
  )
}

export default App
