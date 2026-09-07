import { importLibrary, setOptions } from '@googlemaps/js-api-loader'
import { useEffect, useState } from 'react'

/**
 * Carica Maps JavaScript API una volta sola.
 * La chiave arriva da VITE_GOOGLE_MAPS_API_KEY (.env fuori dal versionamento).
 */
let bootstrap: Promise<[google.maps.MapsLibrary, google.maps.MarkerLibrary]> | null = null

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

export interface MapsApi {
  maps: google.maps.MapsLibrary
  marker: google.maps.MarkerLibrary
}

export function useMapsApi() {
  const [api, setApi] = useState<MapsApi | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let alive = true
    loadLibraries()
      .then(([maps, marker]) => {
        if (alive) {
          setApi({ maps, marker })
        }
      })
      .catch((cause: unknown) => {
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
