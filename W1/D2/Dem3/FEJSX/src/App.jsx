import { useState } from 'react'
import { Campanella } from './Campanella'
import { CanaliPubblici } from './CanaliPubblici'
import { FormInvio } from './FormInvio'
import { ListaNotifiche } from './ListaNotifiche'
import { useNotifiche } from './useNotifiche'

const UTENTI = ['mario', 'lucia']

export default function App() {
  const [utente, setUtente] = useState(UTENTI[0])

  // Gli utenti sono due, quindi "l'altro" e' sempre uno solo:
  // mario scrive a lucia e lucia a mario.
  const destinatario = UTENTI.find((u) => u !== utente) ?? utente

  /**
   * Tutto lo stato dell'applicazione sta qui dentro.
   *
   * Cambiare utente cambia la dipendenza dell'effetto dentro l'hook: la connessione
   * WebSocket si chiude e ne parte una nuova, con un altro ?utente= nell'handshake.
   * Si vede bene nella console del browser.
   */
  const n = useNotifiche(utente)

  return (
    <div className="app">
      <header className="topbar">
        <div className="marchio">
          <span className="logo">🔔</span>
          <div>
            <h1>Centro notifiche</h1>
            <small>WebSocket nativo · instradamento scritto a mano</small>
          </div>
        </div>

        <div className="strumenti">
          <div className="utenti" role="group" aria-label="utente attivo">
            {UTENTI.map((u) => (
              <button
                key={u}
                className={u === utente ? 'utente attivo' : 'utente'}
                onClick={() => setUtente(u)}
              >
                <span className="avatar">{u[0].toUpperCase()}</span>
                {u}
              </button>
            ))}
          </div>

          <span className={n.connesso ? 'stato connesso' : 'stato'}>
            <span className="spia" />
            {n.connesso ? 'connesso' : 'non connesso'}
          </span>

          <Campanella
            notifiche={n.pagina.content}
            nonLette={n.nonLette}
            onApri={n.segnaTutte}
          />
        </div>
      </header>

      {/* Quattro numeri in alto, utili a colpo d'occhio mentre si prova la demo. */}
      <section className="riquadri">
        <article className="riquadro">
          <span className="numero">{n.pagina.totalElements}</span>
          <span className="etichetta">totali</span>
        </article>
        <article className="riquadro accento">
          <span className="numero">{n.nonLette}</span>
          <span className="etichetta">non lette</span>
        </article>
        <article className="riquadro">
          <span className="numero">{n.pagina.totalPages}</span>
          <span className="etichetta">pagine</span>
        </article>
        <article className="riquadro">
          <span className="numero">{n.connesso ? 'ON' : 'OFF'}</span>
          <span className="etichetta">websocket</span>
        </article>
      </section>

      {n.errore && (
        <p className="banda-errore">
          {n.errore}
          <button className="minimo" onClick={() => void n.ricarica()}>
            riprova
          </button>
        </p>
      )}

      <main className="contenuto">
        {/* Crea una notifica passando dal REST: il canale non c'entra. */}
        <FormInvio mittente={utente} destinatario={destinatario} />

        {/* Gli altri due canali: a tutti, e a chi segue un certo ordine. */}
        <CanaliPubblici
          pubbliche={n.pubbliche}
          ordiniSeguiti={n.ordiniSeguiti}
          onSegui={n.segui}
          onSmetti={n.smetti}
          onPubblica={n.pubblica}
          onSvuota={n.svuotaPubbliche}
        />

        {/* Lo storico, che arriva dalle chiamate HTTP e non dal canale. */}
        <ListaNotifiche
          utente={utente}
          pagina={n.pagina}
          dimensione={n.dimensione}
          caricamento={n.caricamento}
          arretrate={n.arretrate}
          onVaiA={n.vaiA}
          onCambiaDimensione={n.cambiaDimensione}
          onSegnaUna={n.segnaUna}
          onSegnaTutte={n.segnaTutte}
        />
      </main>
    </div>
  )
}
