import { useState } from 'react'
import { Map, AdvancedMarker, InfoWindow } from '@vis.gl/react-google-maps'

const LA_SPEZIA = { lat: 44.1025, lng: 9.8241 }

// Prop attese (in TypeScript erano un type Props):
// posizione: { lat, lng } | null - il punto cliccato
// onPosizioneChange: (posizione) => void
// posts: PostResponse[] - vedi api.js
// onBoundsChange: (bounds) => void
function MappaPost({ posizione, onPosizioneChange, posts, onBoundsChange }) {
  // Un solo InfoWindow per tutta la mappa: lo stato non dice "aperto/chiuso",
  // dice QUALE post lo ha aperto. Con un InfoWindow per marker se ne aprirebbero
  // tanti insieme e ognuno avrebbe una copia dello stato.
  const [postSelezionato, setPostSelezionato] = useState(null)

  function handleClick(e) {
    if (e.detail.latLng) {
      onPosizioneChange(e.detail.latLng)
      setPostSelezionato(null)
    }
  }

  // onIdle scatta quando la mappa si e' fermata, non a ogni pixel di trascinamento:
  // con onBoundsChanged partirebbe una richiesta per fotogramma.
  function handleIdle(e) {
    const bounds = e.map.getBounds()
    if (bounds) {
      onBoundsChange(bounds.toJSON())
    }
  }

  return (
    <div style={{ height: '600px', width: '100%' }}>
      <Map
        defaultCenter={LA_SPEZIA}
        defaultZoom={13}
        mapId="DEMO_MAP_ID"
        onClick={handleClick}
        onIdle={handleIdle}
      >
        {posizione && <AdvancedMarker position={posizione} />}

        {posts.map((post) => {
          // Un post senza coordinate non puo' diventare un marker: si filtra qui,
          // prima di provare a disegnarlo.
          if (post.latitude === null || post.longitude === null) {
            return null
          }
          return (
            <AdvancedMarker
              key={post.id}
              position={{ lat: Number(post.latitude), lng: Number(post.longitude) }}
              onClick={() => setPostSelezionato(post)}
            />
          )
        })}

        {postSelezionato && (
          <InfoWindow
            position={{
              lat: Number(postSelezionato.latitude),
              lng: Number(postSelezionato.longitude),
            }}
            onCloseClick={() => setPostSelezionato(null)}
          >
            <strong>{postSelezionato.title}</strong>
            {postSelezionato.formattedAddress && <p>{postSelezionato.formattedAddress}</p>}
          </InfoWindow>
        )}
      </Map>
    </div>
  )
}

export default MappaPost
