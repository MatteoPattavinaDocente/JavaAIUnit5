import { useEffect, useRef, useState } from 'react'
import { quandoFa, TIPI } from './tipi'

// In TSX questo componente dichiarava le sue props in un type:
//   type Props = { notifiche: NotificationDto[]; nonLette: number; onApri: () => Promise<void> }
// Qui restano i tre nomi nella destrutturazione, e la loro forma va cercata in App,
// che e' l'unico posto dove le props vengono passate.

// Nel pannello ne mostriamo poche: e' un'anteprima, l'elenco completo sta sotto.
const ANTEPRIMA = 5

/**
 * La campanella con il pallino rosso e il pannello a scomparsa.
 *
 * Non sa niente di WebSocket ne' di chiamate HTTP: riceve dei dati e delle funzioni
 * e si occupa solo di mostrarli. Tutta la logica sta in useNotifiche.
 */
export function Campanella({ notifiche, nonLette, onApri }) {
  const [aperto, setAperto] = useState(false)

  // Un riferimento all'elemento vero nella pagina, per poter chiedere
  // "il click e' avvenuto dentro di me o fuori?".
  const contenitore = useRef(null)

  /**
   * Un pannello sovrapposto non si chiude da solo quando si clicca altrove:
   * l'ascolto lo mettiamo noi sull'intero documento.
   *
   * Nota la funzione restituita alla fine: toglie gli ascoltatori quando il pannello
   * si chiude. Senza, ogni apertura ne aggiungerebbe due nuovi e resterebbero li'
   * per sempre a consumare memoria.
   */
  useEffect(() => {
    if (!aperto) return

    // In TSX i due parametri erano annotati (e: MouseEvent, e: KeyboardEvent) e il
    // bersaglio andava convertito con "e.target as Node". Senza tipi si scrive di
    // meno, ma l'editor non sa piu' suggerire che cosa contiene l'evento.
    const fuori = (e) => {
      if (!contenitore.current?.contains(e.target)) setAperto(false)
    }
    const esc = (e) => {
      if (e.key === 'Escape') setAperto(false)
    }

    document.addEventListener('mousedown', fuori)
    document.addEventListener('keydown', esc)
    return () => {
      document.removeEventListener('mousedown', fuori)
      document.removeEventListener('keydown', esc)
    }
  }, [aperto])

  const alClick = () => {
    const prossimo = !aperto
    setAperto(prossimo)
    // Aprire il pannello vuol dire "le ho viste": una sola chiamata read-all,
    // non una PATCH per ogni notifica.
    if (prossimo && nonLette > 0) void onApri()
  }

  return (
    <div className="campanella" ref={contenitore}>
      <button
        className="campana"
        onClick={alClick}
        // aria-expanded e aria-label servono a chi naviga con lo screen reader:
        // senza, questo pulsante sarebbe solo un disegno di campanella senza significato.
        aria-expanded={aperto}
        aria-label={`notifiche${nonLette > 0 ? `, ${nonLette} non lette` : ''}`}
      >
        🔔
        {nonLette > 0 && <span className="badge">{nonLette > 99 ? '99+' : nonLette}</span>}
      </button>

      {aperto && (
        <div className="pannello">
          <header className="pannello-testa">
            <strong>Notifiche</strong>
            <span>{nonLette > 0 ? `${nonLette} non lette` : 'tutte lette'}</span>
          </header>

          <ul className="pannello-lista">
            {notifiche.length === 0 && <li className="vuoto">nessuna notifica</li>}
            {notifiche.slice(0, ANTEPRIMA).map((n) => (
              <li key={n.id} className={n.read ? 'letta' : ''}>
                <span className="icona">{TIPI[n.type].icona}</span>
                <span className="testo">
                  <strong>{n.title}</strong>
                  <small>
                    {TIPI[n.type].etichetta}
                    {n.resourceId !== null && ` · #${n.resourceId}`} · {quandoFa(n.createdAt)}
                  </small>
                </span>
              </li>
            ))}
          </ul>

          {notifiche.length > ANTEPRIMA && (
            <footer className="pannello-piede">le altre nella lista qui sotto</footer>
          )}
        </div>
      )}
    </div>
  )
}
