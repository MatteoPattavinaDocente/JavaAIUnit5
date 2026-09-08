// Come si presenta ogni tipo di notifica: icona, etichetta leggibile, classe CSS.
// In TSX quella forma era dichiarata come type Meta = { icona, etichetta, classe };
// qui e' solo la forma degli oggetti scritti qui sotto.

// In TSX questo era un Record<NotificationType, Meta>, e dimenticare un tipo era un
// errore di compilazione. Qui e' un oggetto normale: se domani il backend aggiunge un
// valore all'enum e ce ne dimentichiamo, TIPI[tipo] restituisce undefined e il
// componente che lo usa smette di funzionare in silenzio.
export const TIPI = {
  ORDER_SHIPPED: { icona: '📦', etichetta: 'Ordine spedito', classe: 'spedito' },
  ORDER_CANCELLED: { icona: '🚫', etichetta: 'Ordine annullato', classe: 'annullato' },
  MESSAGE: { icona: '💬', etichetta: 'Messaggio', classe: 'messaggio' },
}

// L'elenco dei tipi, per riempire la tendina del form.
// In TSX la riga finiva con "as NotificationType[]", perche' Object.keys restituisce
// delle stringhe qualunque e andava detto al compilatore che quelle stringhe sono i
// nostri tipi. Senza TypeScript sono stringhe e basta.
export const TIPI_ORDINATI = Object.keys(TIPI)

// Intl e' gia' dentro il browser: sa dire "3 minuti fa" nella lingua che gli chiediamo,
// senza librerie esterne.
const FORMATO = new Intl.RelativeTimeFormat('it', { numeric: 'auto' })

// Le unita' in ordine crescente, ognuna con quante ne stanno in quella dopo:
// 60 secondi in un minuto, 60 minuti in un'ora, 24 ore in un giorno, 7 giorni in una settimana.
const SCALE = [
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
export function quandoFa(iso) {
  let delta = (Date.parse(iso) - Date.now()) / 1000
  for (const [unita, soglia] of SCALE) {
    if (Math.abs(delta) < soglia) return FORMATO.format(Math.round(delta), unita)
    delta /= soglia
  }
  return new Date(iso).toLocaleDateString('it')
}

// Data e ora complete, per il tooltip: "3 minuti fa" e' comodo ma a volte
// serve sapere esattamente quando.
export function oraEsatta(iso) {
  return new Date(iso).toLocaleString('it')
}
