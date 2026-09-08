import { useCallback, useEffect, useState } from 'react'
import { caricaPagina, contaNonLette, segnaLetta, segnaTutteLette, type Pagina } from './api'

/**
 * Tutto lo stato delle notifiche in un posto solo: cosa mostriamo e come lo si aggiorna.
 *
 * E' un custom hook: una funzione che inizia per "use" e che puo' usare gli hook di React
 * (useState, useEffect...). Serve a togliere la logica dai componenti e lasciarli
 * occupare solo di come le cose appaiono.
 *
 * IL PUNTO DELLA DEM 1:
 * qui dentro non c'e' nessun canale aperto verso il backend. Ci sono solo due GET.
 * Finche' qualcuno non chiama ricarica(), il browser non ha nessun modo di sapere
 * che sul database e' nata una notifica nuova.
 */
export function useNotifiche(utente: string) {
  const [pagina, setPagina] = useState<Pagina | null>(null) // null = non abbiamo ancora caricato
  const [numeroPagina, setNumeroPagina] = useState(0)
  const [nonLette, setNonLette] = useState(0)
  const [ultimoAggiornamento, setUltimoAggiornamento] = useState<Date | null>(null)

  /**
   * L'unica funzione che va a chiedere i dati al backend.
   *
   * Le due chiamate partono insieme con Promise.all invece che una dopo l'altra:
   * sono indipendenti, quindi aspettarle in fila sarebbe tempo buttato.
   *
   * useCallback dice a React: "ricrea questa funzione solo se cambia l'utente o la pagina".
   * Senza, verrebbe ricreata a ogni render e l'useEffect qui sotto ripartirebbe all'infinito.
   */
  const ricarica = useCallback(async () => {
    const [p, conteggio] = await Promise.all([caricaPagina(utente, numeroPagina), contaNonLette(utente)])
    setPagina(p)
    setNonLette(conteggio)
    setUltimoAggiornamento(new Date())
  }, [utente, numeroPagina])

  // Carica i dati all'apertura della pagina, e ogni volta che cambia utente o pagina.
  // Il "void" davanti serve solo a dire a TypeScript che sappiamo di stare ignorando
  // la promise restituita: useEffect non puo' aspettarla.
  useEffect(() => {
    void ricarica()
  }, [ricarica])

  // Dopo aver scritto sul backend ricarichiamo, altrimenti la pagina resta indietro.
  // E' il modo piu' semplice, e per questa demo va benissimo.
  const leggi = async (id: number) => {
    await segnaLetta(id)
    await ricarica()
  }

  const leggiTutte = async () => {
    await segnaTutteLette(utente)
    await ricarica()
  }

  return {
    pagina,
    numeroPagina,
    nonLette,
    ultimoAggiornamento,
    ricarica,
    leggi,
    leggiTutte,
    vaiA: setNumeroPagina, // rinominato per leggibilita': onVaiA(2) si capisce meglio di setNumeroPagina(2)
  }
}

// Il tipo di quello che l'hook restituisce, ricavato da TypeScript senza riscriverlo a mano.
// Serve al componente Campanella per dichiarare cosa riceve.
export type StatoNotifiche = ReturnType<typeof useNotifiche>
