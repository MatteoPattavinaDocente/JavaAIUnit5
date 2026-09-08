import type { NotificationType } from './api'

// Come si presenta ogni tipo di notifica: icona, etichetta leggibile, classe CSS.
type Meta = { icona: string; etichetta: string; classe: string }

// Record<Chiave, Valore> obbliga a coprire tutti i tipi: se domani il backend
// aggiunge un valore all'enum e ce ne dimentichiamo qui, TypeScript protesta.
export const TIPI: Record<NotificationType, Meta> = {
  ORDER_SHIPPED: { icona: '📦', etichetta: 'Ordine spedito', classe: 'spedito' },
  ORDER_CANCELLED: { icona: '🚫', etichetta: 'Ordine annullato', classe: 'annullato' },
  MESSAGE: { icona: '💬', etichetta: 'Messaggio', classe: 'messaggio' },
}

// L'elenco dei tipi, per riempire la tendina del form.
export const TIPI_ORDINATI = Object.keys(TIPI) as NotificationType[]

// Intl e' gia' dentro il browser: sa dire "3 minuti fa" nella lingua che gli chiediamo,
// senza librerie esterne.
const FORMATO = new Intl.RelativeTimeFormat('it', { numeric: 'auto' })

// Le unita' in ordine crescente, ognuna con quante ne stanno in quella dopo:
// 60 secondi in un minuto, 60 minuti in un'ora, 24 ore in un giorno, 7 giorni in una settimana.
const SCALE: [Intl.RelativeTimeFormatUnit, number][] = [
  ['second', 60],
  ['minute', 60],
  ['hour', 24],
  ['day', 7],
]

/**
 * Trasforma "2025-03-14T10:22:31Z" in "3 minuti fa".
 *
 * Parte dai secondi e sale di unita' finche' il numero non diventa abbastanza piccolo
 * da essere leggibile: 90 secondi diventano "2 minuti fa", non "90 secondi fa".
 * Oltre la settimana si arrende e mostra la data.
 *
 * createdAt arriva in UTC: Date lo riporta nel fuso locale da solo, qui serve solo
 * la distanza da adesso.
 */
export function quandoFa(iso: string): string {
  let delta = (Date.parse(iso) - Date.now()) / 1000
  for (const [unita, soglia] of SCALE) {
    if (Math.abs(delta) < soglia) return FORMATO.format(Math.round(delta), unita)
    delta /= soglia
  }
  return new Date(iso).toLocaleDateString('it')
}

// Data e ora complete, per il tooltip: "3 minuti fa" e' comodo ma a volte
// serve sapere esattamente quando.
export function oraEsatta(iso: string): string {
  return new Date(iso).toLocaleString('it')
}
