import { useState } from 'react'
import type { NotificationDto } from './api'

type Props = {
  notifiche: NotificationDto[]
  nonLette: number
  onApri: () => Promise<void>
}

/**
 * La campanella con il pallino rosso.
 *
 * E' volutamente piu' spartana di quella della Dem 3: qui la lezione non e'
 * l'interfaccia, e' il trasporto. Serve solo a vedere il badge accendersi
 * (o non accendersi, se il CONNECT e' partito senza login).
 */
export function Campanella({ notifiche, nonLette, onApri }: Props) {
  const [aperto, setAperto] = useState(false)

  const alClick = () => {
    const prossimo = !aperto
    setAperto(prossimo)
    // Aprire il pannello vuol dire "le ho viste": una sola chiamata read-all,
    // non una per notifica.
    if (prossimo && nonLette > 0) void onApri()
  }

  return (
    <div className="campanella">
      <button onClick={alClick}>
        🔔
        {nonLette > 0 && <span className="badge">{nonLette}</span>}
      </button>

      {aperto && (
        <ul className="pannello">
          {notifiche.length === 0 && <li className="vuoto">nessuna notifica</li>}
          {notifiche.map((n) => (
            <li key={n.id} className={n.read ? 'letta' : ''}>
              <strong>{n.title}</strong>
              <small>
                {n.type} · ordine {n.resourceId} · {new Date(n.createdAt).toLocaleTimeString()}
              </small>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
