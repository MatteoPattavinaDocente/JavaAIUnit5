import { useState } from 'react'

/**
 * La campanella: il pulsante con il pallino rosso e il pannello che si apre.
 *
 * Riceve un oggetto solo, quello restituito da useNotifiche, invece di otto props separate.
 * Meno righe da leggere e, se domani l'hook aggiunge un campo, qui non si tocca niente.
 *
 * Questo componente non sa niente di fetch o di URL: mostra quello che gli arriva
 * e chiama le funzioni che gli arrivano. Tutta la logica sta nell'hook.
 *
 * In TSX la firma dichiarava anche la forma delle props, cosi':
 *   export function Campanella({ notifiche }: { notifiche: StatoNotifiche })
 * Qui la destrutturazione e' nuda. Il vantaggio e' che si legge in fretta, il prezzo
 * e' che un nome sbagliato come notifiche.nonLete non lo segnala nessuno: in pagina
 * comparirebbe uno spazio vuoto e toccherebbe a noi capire perche'.
 */
export function Campanella({ notifiche }) {
  // Lo stato "pannello aperto o chiuso" riguarda solo questo componente,
  // quindi vive qui e non nell'hook.
  const [aperto, setAperto] = useState(false)

  const { pagina, numeroPagina, nonLette, ultimoAggiornamento } = notifiche

  return (
    <div className="campanella">
      <button className="bottone-campanella" onClick={() => setAperto(!aperto)}>
        🔔
        {/* Il badge compare solo se c'e' qualcosa da leggere.
            In JSX "condizione && elemento" significa: se la condizione e' falsa, non disegnare niente. */}
        {nonLette > 0 && <span className="badge">{nonLette}</span>}
      </button>

      {aperto && (
        <section className="pannello">
          <header>
            <strong>Notifiche</strong>
            <span className="non-lette">{nonLette} da leggere</span>
            <button onClick={() => void notifiche.ricarica()}>Aggiorna</button>
            <button onClick={() => void notifiche.leggiTutte()} disabled={nonLette === 0}>
              Segna tutte lette
            </button>
          </header>

          <ul>
            {pagina?.content.length === 0 && <li className="vuoto">nessuna notifica</li>}

            {/* map trasforma ogni notifica in un <li>.
                La key serve a React per capire quale riga e' quale quando la lista cambia. */}
            {pagina?.content.map((n) => (
              <li key={n.id} className={n.read ? 'letta' : 'da-leggere'}>
                <span className="tipo">{ETICHETTE[n.type]}</span>
                <span className="titolo">{n.title}</span>
                {/* createdAt arriva come stringa UTC: qui la trasformiamo nel fuso dell'utente. */}
                <span className="quando">{new Date(n.createdAt).toLocaleString('it-IT')}</span>
                {!n.read && (
                  <button className="segna" onClick={() => void notifiche.leggi(n.id)}>
                    segna letta
                  </button>
                )}
              </li>
            ))}
          </ul>

          <footer>
            <button onClick={() => notifiche.vaiA(numeroPagina - 1)} disabled={numeroPagina === 0}>
              ← precedenti
            </button>
            <span>
              {/* pagina.number parte da 0, gli umani contano da 1: +1 solo per mostrarlo. */}
              pagina {(pagina?.number ?? 0) + 1} di {pagina?.totalPages ?? 1} · {pagina?.totalElements ?? 0} in tutto
            </span>
            <button
              onClick={() => notifiche.vaiA(numeroPagina + 1)}
              disabled={pagina === null || numeroPagina + 1 >= pagina.totalPages}
            >
              successive →
            </button>
          </footer>

          <p className="avviso">
            Ultimo aggiornamento: {ultimoAggiornamento?.toLocaleTimeString('it-IT') ?? 'mai'}.
            Questi dati non si muovono da soli: cambiano solo quando premi «Aggiorna».
          </p>
        </section>
      )}
    </div>
  )
}

// L'etichetta da mostrare per ogni tipo di notifica.
// In TSX era dichiarato come Record<NotificationDto['type'], string>, e dimenticare
// un tipo era un errore di compilazione. Qui e' un oggetto normale: se il backend
// aggiunge un valore all'enum e ce ne dimentichiamo, ETICHETTE[n.type] restituisce
// undefined e la riga compare senza etichetta, senza che nessuno protesti.
const ETICHETTE = {
  ORDER_SHIPPED: '📦 spedito',
  ORDER_CANCELLED: '✖ annullato',
  MESSAGE: '✉ messaggio',
}
