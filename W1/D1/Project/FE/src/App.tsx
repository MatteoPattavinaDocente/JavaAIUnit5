import { useCallback, useEffect, useRef, useState } from 'react'

import { ApiError, fetchReports } from './api'
import { AddressSearch } from './components/AddressSearch'
import { MapView, type MapFocus } from './components/MapView'
import { ReportForm } from './components/ReportForm'
import { ReportList } from './components/ReportList'
import type { Category, Report, Viewport } from './types'
import { useMapsApi } from './useMapsApi'

/** Milano, Piazza Duomo: zoom 14 = quartiere ben leggibile. */
const INITIAL_CENTER: google.maps.LatLngLiteral = { lat: 45.4642, lng: 9.19 }
const INITIAL_ZOOM = 14

export default function App() {
  const { api, error: mapsError } = useMapsApi()

  const [viewport, setViewport] = useState<Viewport | null>(null)
  const [category, setCategory] = useState<Category | null>(null)
  const [reports, setReports] = useState<Report[]>([])
  const [loading, setLoading] = useState(false)
  const [listError, setListError] = useState<string | null>(null)

  const [draftPin, setDraftPin] = useState<google.maps.LatLngLiteral | null>(null)
  const [focus, setFocus] = useState<MapFocus | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)

  // Ogni 'idle' della mappa rifa' la query: la lista segue sempre il riquadro visibile.
  const requestId = useRef(0)
  useEffect(() => {
    if (!viewport) {
      return
    }
    const controller = new AbortController()
    const current = ++requestId.current
    setLoading(true)
    fetchReports(viewport, category, controller.signal)
      .then((data) => {
        if (current === requestId.current) {
          setReports(data)
          setListError(null)
        }
      })
      .catch((cause: unknown) => {
        if (controller.signal.aborted || current !== requestId.current) {
          return
        }
        setListError(cause instanceof ApiError ? cause.message : 'Caricamento non riuscito')
      })
      .finally(() => {
        if (current === requestId.current) {
          setLoading(false)
        }
      })
    return () => controller.abort()
  }, [viewport, category])

  const handleViewportChange = useCallback((next: Viewport) => {
    setViewport(next)
  }, [])

  const handleMapClick = useCallback((position: google.maps.LatLngLiteral) => {
    setDraftPin(position)
  }, [])

  const handleCreated = useCallback((report: Report) => {
    setDraftPin(null)
    setReports((previous) => [report, ...previous.filter((item) => item.id !== report.id)])
    setFocus({ latitude: report.latitude, longitude: report.longitude })
    setSelectedId(report.id)
  }, [])

  return (
    <div className="layout">
      <aside className="sidebar">
        <header className="brand">
          <h1>Segnalazioni urbane</h1>
          <p>Clicca sulla mappa per segnalare un problema.</p>
        </header>

        <AddressSearch
          onFound={(result) => {
            setFocus({ latitude: result.latitude, longitude: result.longitude, zoom: 16 })
            setDraftPin({ lat: result.latitude, lng: result.longitude })
          }}
        />

        {draftPin && (
          <ReportForm
            position={draftPin}
            onCreated={handleCreated}
            onCancel={() => setDraftPin(null)}
          />
        )}

        <ReportList
          reports={reports}
          loading={loading}
          error={listError}
          category={category}
          selectedId={selectedId}
          onCategoryChange={setCategory}
          onSelect={(report) => {
            setFocus({ latitude: report.latitude, longitude: report.longitude })
            setSelectedId(report.id)
          }}
        />
      </aside>

      <main className="map-area">
        {mapsError && <div className="map-error">{mapsError}</div>}
        {api && (
          <MapView
            api={api}
            center={INITIAL_CENTER}
            zoom={INITIAL_ZOOM}
            reports={reports}
            focus={focus}
            selectedId={selectedId}
            draftPin={draftPin}
            onViewportChange={handleViewportChange}
            onMapClick={handleMapClick}
            onMarkerSelect={setSelectedId}
          />
        )}
        {!api && !mapsError && <div className="map-error">Caricamento della mappa...</div>}
      </main>
    </div>
  )
}
