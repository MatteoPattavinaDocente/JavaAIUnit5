import { importLibrary, setOptions } from '@googlemaps/js-api-loader'
import { useEffect, useState } from 'react'

/**
 * Carica Maps JavaScript API una volta sola.
 * La chiave arriva da VITE_GOOGLE_MAPS_API_KEY (.env fuori dal versionamento).
 */
let bootstrap = null

function loadLibraries() {
  if (!bootstrap) {
    const key = import.meta.env.VITE_GOOGLE_MAPS_API_KEY
    if (!key) {
      return Promise.reject(new Error('VITE_GOOGLE_MAPS_API_KEY non impostata: copiare .env.example in .env'))
    }
    setOptions({ key, v: 'weekly', language: 'it', region: 'IT' })
    bootstrap = Promise.all([importLibrary('maps'), importLibrary('marker')])
  }
  return bootstrap
}

/**
 * Le due librerie caricate: maps (Map, InfoWindow) e marker (AdvancedMarkerElement, PinElement).
 * @typedef {object} MapsApi
 * @property {google.maps.MapsLibrary} maps
 * @property {google.maps.MarkerLibrary} marker
 */

export function useMapsApi() {
  const [api, setApi] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    // alive evita di chiamare setState dopo lo smontaggio: la promise puo'
    // risolversi quando il componente non c'e' piu'.
    let alive = true
    loadLibraries()
      .then(([maps, marker]) => {
        if (alive) {
          setApi({ maps, marker })
        }
      })
      .catch((cause) => {
        if (alive) {
          setError(cause instanceof Error ? cause.message : 'Caricamento di Google Maps non riuscito')
        }
      })
    return () => {
      alive = false
    }
  }, [])

  return { api, error }
}
