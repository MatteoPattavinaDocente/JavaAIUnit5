import { useState } from 'react'
import { spedisciOrdine } from './api'
import { Campanella } from './Campanella'
import { useNotifiche } from './useNotifiche'

// Due utenti finti per poter mostrare a lezione che le notifiche
// di mario non finiscono a lucia.
const UTENTI = ['mario', 'lucia']

export default function App() {
  // Chi sta guardando la pagina. In un'app vera arriverebbe dal login:
  // qui e' una tendina, cosi' possiamo cambiare utente al volo.
  const [utente, setUtente] = useState(UTENTI[0])

  // Il numero dell'ordine da "spedire". Cresce a ogni click cosi'
  // ogni notifica ha un titolo diverso e si distinguono nella lista.
  const [ordine, setOrdine] = useState(42)

  // Tutto lo stato delle notifiche arriva da qui. Cambiando utente,
  // l'hook ricarica da solo i dati del nuovo destinatario.
  const notifiche = useNotifiche(utente)

  const spedisci = async () => {
    await spedisciOrdine(ordine, utente)
    setOrdine(ordine + 1)
    // Nota: NON chiamiamo notifiche.ricarica(). E' voluto.
    // Serve a far vedere che senza ricaricare, la pagina resta indietro.
  }

  return (
    <main>
      <header className="barra">
        <h1>Notifiche</h1>

        <select value={utente} onChange={(e) => setUtente(e.target.value)}>
          {UTENTI.map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>

        <Campanella notifiche={notifiche} />
      </header>

      <section className="telecomando">
        <h2>Telecomando della lezione</h2>
        <p>
          Genera una notifica per <strong>{utente}</strong> chiamando l'endpoint di comodo del backend.
        </p>
        <button onClick={() => void spedisci()}>Spedisci l'ordine {ordine}</button>
        <p className="nota">
          Apri la campanella dopo aver premuto: il badge non e' cambiato. La riga e' in tabella,
          ma nessuno ha avvisato il browser. Serve «Aggiorna».
        </p>
      </section>
    </main>
  )
}
