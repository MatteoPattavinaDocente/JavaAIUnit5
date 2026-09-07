// LocationPicker: cliccando sulla mappa compare un marker e vengono stampate le
// sue coordinate. Il bottone finale cancella la scelta.
// Versione JSX: stesso comportamento della gemella .tsx, senza annotazioni di tipo.

// useState da' una memoria al componente: conserva il valore fra un render e
// l'altro e, quando lo modifichiamo, avvisa React che deve ridisegnare.
import { useState } from 'react'

// Componenti React che avvolgono l'SDK di Google Maps: Map e' il riquadro della
// mappa, AdvancedMarker lo spillo. In TypeScript importavamo anche il tipo
// MapMouseEvent; qui non serve, l'evento arriva senza etichetta.
// La mappa funziona solo se piu' in alto nell'albero c'e' un <APIProvider>.
import { Map, AdvancedMarker } from '@vis.gl/react-google-maps'

// Oggetto { lat, lng }: la forma che Google Maps si aspetta.
// Sta fuori dal componente perche' e' un valore fisso, inutile ricrearlo.
const LA_SPEZIA = { lat: 44.1025, lng: 9.8241 }

function LocationPicker() {
  // Unica fonte di verita': marker, testo e bottone dipendono tutti da qui.
  // null significa "nessuna posizione scelta". In JavaScript basta il valore
  // iniziale: non c'e' nessun tipo da dichiarare, ma nessuno ci avverte se
  // mettiamo dentro qualcosa che non ha lat e lng.
  const [posizione, setPosizione] = useState(null)

  // La libreria richiama questa funzione a ogni click, passandole l'evento.
  function handleClick(e) {
    // Le coordinate stanno in e.detail.latLng, non in e.latLng: chi sbaglia
    // ottiene undefined senza nessun errore in console.
    // L'if serve perche' latLng puo' mancare (click fuori dalla mappa disegnata).
    if (e.detail.latLng) {
      // Cambiare stato fa ripartire il render: marker e testo si aggiornano da
      // soli, senza toccare il DOM a mano.
      setPosizione(e.detail.latLng)
    }
  }

  return (
    <div>
      {/* Map riempie il contenitore, quindi serve un'altezza esplicita: senza,
          il div collassa a 0px e la mappa resta invisibile. */}
      <div style={{ height: '600px', width: '100%' }}>
        {/* Le prop "default" valgono solo al primo render e lasciano pan e zoom
            all'utente; con center e zoom la vista tornerebbe qui ogni volta.
            mapId indica lo stile della mappa ed e' obbligatorio per AdvancedMarker. */}
        <Map
          defaultCenter={LA_SPEZIA}
          defaultZoom={12}
          mapId="DEMO_MAP_ID"
          onClick={handleClick}
        >
          {/* Un solo marker, con position legata allo stato: sembra spostarsi
              perche' non esiste una lista in cui un secondo possa nascere.
              "condizione && elemento": se posizione e' null React non disegna nulla. */}
          {posizione && <AdvancedMarker position={posizione} />}
        </Map>
      </div>

      {/* Sei decimali equivalgono a circa 10 cm di precisione. Usiamo il ternario
          e non && perche' anche senza posizione vogliamo mostrare un messaggio. */}
      {posizione ? (
        <p>
          Lat: {posizione.lat.toFixed(6)} — Lng: {posizione.lng.toFixed(6)}
        </p>
      ) : (
        <p>Clicca sulla mappa per scegliere una posizione.</p>
      )}

      {/* onClick vuole una funzione, da qui la arrow function: scrivendo
          setPosizione(null) senza freccia partirebbe durante il render.
          disabled disattiva il bottone quando non c'e' nulla da rimuovere. */}
      <button onClick={() => setPosizione(null)} disabled={!posizione}>
        Rimuovi posizione
      </button>
    </div>
  )
}

export default LocationPicker
