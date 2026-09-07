import { MarkerClusterer } from '@googlemaps/markerclusterer'
import { useEffect, useRef } from 'react'

import type { MapsApi } from '../useMapsApi'
import { CATEGORY_COLOR, CATEGORY_LABEL, type Report, type Viewport } from '../types'

/** Sopra questa soglia i marker vengono raggruppati (bonus clustering). */
const CLUSTER_THRESHOLD = 50

export interface MapFocus {
  latitude: number
  longitude: number
  zoom?: number
}

interface Props {
  api: MapsApi
  center: google.maps.LatLngLiteral
  zoom: number
  reports: Report[]
  focus: MapFocus | null
  selectedId: number | null
  draftPin: google.maps.LatLngLiteral | null
  onViewportChange: (viewport: Viewport) => void
  onMapClick: (position: google.maps.LatLngLiteral) => void
  onMarkerSelect: (id: number | null) => void
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString('it-IT', { dateStyle: 'medium', timeStyle: 'short' })
}

/** Contenuto della InfoWindow costruito come DOM: nessuna interpolazione di HTML. */
function buildInfoContent(report: Report): HTMLElement {
  const root = document.createElement('div')
  root.className = 'info-window'

  const badge = document.createElement('span')
  badge.className = 'info-badge'
  badge.style.backgroundColor = CATEGORY_COLOR[report.category]
  badge.textContent = CATEGORY_LABEL[report.category]
  root.append(badge)

  const description = document.createElement('p')
  description.className = 'info-description'
  description.textContent = report.description
  root.append(description)

  if (report.address) {
    const address = document.createElement('p')
    address.className = 'info-address'
    address.textContent = report.address
    root.append(address)
  }

  const date = document.createElement('time')
  date.className = 'info-date'
  date.dateTime = report.createdAt
  date.textContent = formatDate(report.createdAt)
  root.append(date)

  return root
}

export function MapView({
  api,
  center,
  zoom,
  reports,
  focus,
  selectedId,
  draftPin,
  onViewportChange,
  onMapClick,
  onMarkerSelect,
}: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const mapRef = useRef<google.maps.Map | null>(null)
  // Una sola InfoWindow condivisa da tutti i marker.
  const infoWindowRef = useRef<google.maps.InfoWindow | null>(null)
  const markersRef = useRef(new Map<number, google.maps.marker.AdvancedMarkerElement>())
  const clustererRef = useRef<MarkerClusterer | null>(null)
  const draftMarkerRef = useRef<google.maps.marker.AdvancedMarkerElement | null>(null)

  // I callback cambiano identita' ad ogni render: li leggo da ref per non riagganciare i listener.
  const callbacks = useRef({ onViewportChange, onMapClick, onMarkerSelect })
  callbacks.current = { onViewportChange, onMapClick, onMarkerSelect }

  // --- creazione mappa (una volta sola) ---
  useEffect(() => {
    if (mapRef.current || !containerRef.current) {
      return
    }
    const map = new api.maps.Map(containerRef.current, {
      center,
      zoom,
      // mapId obbligatorio per AdvancedMarkerElement
      mapId: import.meta.env.VITE_GOOGLE_MAPS_MAP_ID ?? 'DEMO_MAP_ID',
      clickableIcons: false,
      mapTypeControl: false,
      streetViewControl: false,
      fullscreenControl: false,
    })
    mapRef.current = map
    infoWindowRef.current = new api.maps.InfoWindow({ maxWidth: 280 })

    map.addListener('idle', () => {
      const bounds = map.getBounds()
      if (!bounds) {
        return
      }
      const sw = bounds.getSouthWest()
      const ne = bounds.getNorthEast()
      callbacks.current.onViewportChange({
        swLat: sw.lat(),
        swLng: sw.lng(),
        neLat: ne.lat(),
        neLng: ne.lng(),
      })
    })

    map.addListener('click', (event: google.maps.MapMouseEvent) => {
      if (!event.latLng) {
        return
      }
      infoWindowRef.current?.close()
      callbacks.current.onMarkerSelect(null)
      callbacks.current.onMapClick({ lat: event.latLng.lat(), lng: event.latLng.lng() })
    })
    // center/zoom sono solo iniziali: la mappa poi vive di vita propria
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [api])

  // --- marker: ricostruiti quando cambia l'insieme visibile ---
  useEffect(() => {
    const map = mapRef.current
    if (!map) {
      return
    }
    const { AdvancedMarkerElement, PinElement } = api.marker

    clustererRef.current?.clearMarkers()
    markersRef.current.forEach((marker) => {
      marker.map = null
    })
    markersRef.current.clear()

    const created = reports.map((report) => {
      const pin = new PinElement({
        background: CATEGORY_COLOR[report.category],
        borderColor: '#ffffff',
        glyphColor: '#ffffff',
        scale: 1,
      })
      const marker = new AdvancedMarkerElement({
        position: { lat: report.latitude, lng: report.longitude },
        content: pin.element,
        title: `${CATEGORY_LABEL[report.category]} - ${report.description}`,
        gmpClickable: true,
      })
      marker.addListener('gmp-click', () => {
        const infoWindow = infoWindowRef.current
        if (!infoWindow) {
          return
        }
        infoWindow.close()
        infoWindow.setContent(buildInfoContent(report))
        infoWindow.open({ map, anchor: marker })
        callbacks.current.onMarkerSelect(report.id)
      })
      markersRef.current.set(report.id, marker)
      return marker
    })

    if (created.length > CLUSTER_THRESHOLD) {
      if (!clustererRef.current) {
        clustererRef.current = new MarkerClusterer({ map })
      }
      clustererRef.current.addMarkers(created)
    }
    else {
      created.forEach((marker) => {
        marker.map = map
      })
    }
  }, [api, reports])

  // --- marker temporaneo del punto cliccato ---
  useEffect(() => {
    const map = mapRef.current
    if (!map) {
      return
    }
    if (draftMarkerRef.current) {
      draftMarkerRef.current.map = null
      draftMarkerRef.current = null
    }
    if (!draftPin) {
      return
    }
    const pin = new api.marker.PinElement({
      background: '#1a73e8',
      borderColor: '#ffffff',
      glyphColor: '#ffffff',
      scale: 1.2,
    })
    draftMarkerRef.current = new api.marker.AdvancedMarkerElement({
      position: draftPin,
      content: pin.element,
      map,
      title: 'Nuova segnalazione',
      zIndex: 999,
    })
  }, [api, draftPin])

  // --- ricentratura richiesta da fuori (geocoding, click sulla lista) ---
  useEffect(() => {
    const map = mapRef.current
    if (!map || !focus) {
      return
    }
    map.panTo({ lat: focus.latitude, lng: focus.longitude })
    if (focus.zoom) {
      map.setZoom(focus.zoom)
    }
  }, [focus])

  // --- selezione dalla lista: apre la stessa InfoWindow condivisa ---
  useEffect(() => {
    const map = mapRef.current
    const infoWindow = infoWindowRef.current
    if (!map || !infoWindow) {
      return
    }
    if (selectedId === null) {
      infoWindow.close()
      return
    }
    const marker = markersRef.current.get(selectedId)
    const report = reports.find((candidate) => candidate.id === selectedId)
    if (!marker || !report) {
      return
    }
    infoWindow.setContent(buildInfoContent(report))
    infoWindow.open({ map, anchor: marker })
  }, [selectedId, reports])

  return <div className="map" ref={containerRef} />
}
