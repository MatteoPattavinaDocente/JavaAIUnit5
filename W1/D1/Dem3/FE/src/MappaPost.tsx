import { useState } from 'react'
import { Map, AdvancedMarker, InfoWindow, type MapMouseEvent, type MapEvent } from '@vis.gl/react-google-maps'
import type { Bounds, PostResponse } from './api'

const LA_SPEZIA: google.maps.LatLngLiteral = { lat: 44.1025, lng: 9.8241 }

type Props = {
  posizione: google.maps.LatLngLiteral | null
  onPosizioneChange: (posizione: google.maps.LatLngLiteral | null) => void
  posts: PostResponse[]
  onBoundsChange: (bounds: Bounds) => void
}

function MappaPost({ posizione, onPosizioneChange, posts, onBoundsChange }: Props) {
  // Un solo InfoWindow per tutta la mappa: lo stato non dice "aperto/chiuso",
  // dice QUALE post lo ha aperto. Con un InfoWindow per marker se ne aprirebbero
  // tanti insieme e ognuno avrebbe una copia dello stato.
  const [postSelezionato, setPostSelezionato] = useState<PostResponse | null>(null)

  function handleClick(e: MapMouseEvent) {
    if (e.detail.latLng) {
      onPosizioneChange(e.detail.latLng)
      setPostSelezionato(null)
    }
  }

  // onIdle scatta quando la mappa si e' fermata, non a ogni pixel di trascinamento:
  // con onBoundsChanged partirebbe una richiesta per fotogramma.
  function handleIdle(e: MapEvent) {
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
