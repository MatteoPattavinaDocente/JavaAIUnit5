import { useEffect, useRef, useState } from 'react'

// Lato lungo massimo dell'immagine inviata: per l'OCR ~2000px bastano.
const MAX_SIDE = 2000
const JPEG_QUALITY = 0.85

// Props: { onCapture: (file: File) => void }

export default function Camera({ onCapture }) {
  const videoRef = useRef(null)
  const streamRef = useRef(null)
  const [errore, setErrore] = useState(null)
  const [attiva, setAttiva] = useState(false)

  useEffect(() => {
    let annullato = false

    async function avvia() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: 'environment', width: { ideal: 1920 } },
        })
        if (annullato) {
          stream.getTracks().forEach((t) => t.stop())
          return
        }
        streamRef.current = stream
        if (videoRef.current) videoRef.current.srcObject = stream
        setAttiva(true)
      } catch (e) {
        setErrore(descrivi(e))
      }
    }

    avvia()

    // Senza questo la spia della fotocamera resta accesa.
    return () => {
      annullato = true
      streamRef.current?.getTracks().forEach((t) => t.stop())
      streamRef.current = null
    }
  }, [])

  function scatta() {
    const video = videoRef.current
    if (!video) return

    // Riduzione: si disegna il fotogramma gia' in scala sulla canvas.
    const scala = Math.min(1, MAX_SIDE / Math.max(video.videoWidth, video.videoHeight))
    const canvas = document.createElement('canvas')
    canvas.width = Math.round(video.videoWidth * scala)
    canvas.height = Math.round(video.videoHeight * scala)
    canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height)

    canvas.toBlob(
      (blob) => {
        if (blob) onCapture(new File([blob], 'scatto.jpg', { type: 'image/jpeg' }))
      },
      'image/jpeg',
      JPEG_QUALITY,
    )
  }

  if (errore) {
    // Se la fotocamera non e' disponibile l'applicazione deve restare usabile.
    return (
      <section className="card errore">
        <p>Fotocamera non disponibile: {errore}</p>
        <input
          type="file"
          accept="image/*"
          capture="environment"
          onChange={(e) => {
            const file = e.target.files?.[0]
            if (file) onCapture(file)
          }}
        />
      </section>
    )
  }

  return (
    <section className="card">
      <video ref={videoRef} autoPlay playsInline muted style={{ width: '100%', background: '#000' }} />
      <button onClick={scatta} disabled={!attiva}>
        Scatta
      </button>
    </section>
  )
}

function descrivi(e) {
  if (e instanceof DOMException) {
    if (e.name === 'NotAllowedError') return 'permesso negato'
    if (e.name === 'NotFoundError') return 'nessuna fotocamera trovata'
    return e.name
  }
  return String(e)
}
