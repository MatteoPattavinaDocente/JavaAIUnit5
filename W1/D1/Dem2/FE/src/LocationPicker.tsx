import { Map, AdvancedMarker, type MapMouseEvent } from '@vis.gl/react-google-maps'

const LA_SPEZIA: google.maps.LatLngLiteral = { lat: 44.1025, lng: 9.8241 }

// Rispetto alla dimostrazione 1 lo stato non e' piu' qui: e' salito in App, perche'
// ora serve anche al form. Il componente riceve il valore e la funzione per cambiarlo.
type Props = {
  posizione: google.maps.LatLngLiteral | null
  onChange: (posizione: google.maps.LatLngLiteral | null) => void
}

function LocationPicker({ posizione, onChange }: Props) {
  function handleClick(e: MapMouseEvent) {
    // Il payload sta in e.detail.latLng, non in e.
    if (e.detail.latLng) {
      onChange(e.detail.latLng)
    }
  }

  return (
    <div>
      {/* Map riempie il genitore: senza altezza esplicita il div collassa a 0px. */}
      <div style={{ height: '600px', width: '100%' }}>
        <Map
          defaultCenter={LA_SPEZIA}
          defaultZoom={13}
          mapId="DEMO_MAP_ID"
          onClick={handleClick}
        >
          {posizione && <AdvancedMarker position={posizione} />}
        </Map>
      </div>

      {/* Sei decimali: circa 10 cm alle nostre latitudini. */}
      {posizione ? (
        <p>
          Lat: {posizione.lat.toFixed(6)} — Lng: {posizione.lng.toFixed(6)}
        </p>
      ) : (
        <p>Clicca sulla mappa per scegliere una posizione.</p>
      )}

      <button onClick={() => onChange(null)} disabled={!posizione}>
        Rimuovi posizione
      </button>
    </div>
  )
}

export default LocationPicker
