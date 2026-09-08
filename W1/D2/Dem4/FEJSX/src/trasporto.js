/**
 * Le due leve della dimostrazione sul ripiego (fallback), in un posto solo.
 *
 * Sono due cose diverse e vanno tenute distinte, perche' a lezione si confondono
 * di continuo:
 *
 * 1. MODALITA' WS — rompiamo il WebSocket del browser e stiamo a guardare SockJS:
 *    prova, fallisce, ripiega su HTTP da solo. E' il ripiego VERO, quello che in
 *    produzione scatta dietro un proxy che rifiuta l'upgrade a WebSocket.
 *    Costa un ricaricamento della pagina, e il motivo e' spiegato in index.html.
 *
 * 2. TRASPORTO SCELTO — passiamo a SockJS l'opzione transports. Qui non ripiega
 *    nessuno e non si prova niente: gli diciamo quale trasporto usare, punto.
 *    Con 'xhr-streaming' il WebSocket non viene nemmeno tentato.
 *    Non serve ricaricare, basta rifare la connessione.
 *
 * In breve: la prima e' "il WebSocket non funziona, arrangiati";
 * la seconda e' "non usare il WebSocket, te lo dico io".
 */

// In TSX i valori ammessi erano scritti in due tipi, e sbagliarne uno era un errore
// di compilazione:
//
//   ModalitaWs       'normale' | 'websocket-rotto'
//   TrasportoScelto  'auto' | 'websocket' | 'xhr-streaming' | 'xhr-polling'
//
// Senza TypeScript sono stringhe qualunque: le funzioni qui sotto si difendono da
// sole, confrontando il valore letto con quelli che conoscono e ripiegando sul
// valore predefinito quando non lo riconoscono.

// Usiamo sessionStorage e non useState perche' la prima leva ricarica la pagina:
// lo stato di React sparirebbe, questo no. Si svuota da solo chiudendo la scheda.
// Questa chiave la legge anche lo script dentro index.html, prima del bundle.
const CHIAVE_WS = 'ws-modalita'
const CHIAVE_TRASPORTO = 'trasporto-scelto'

export function leggiModalitaWs() {
  return sessionStorage.getItem(CHIAVE_WS) === 'websocket-rotto' ? 'websocket-rotto' : 'normale'
}

/**
 * Scrive la scelta e ricarica la pagina.
 *
 * Il ricaricamento non e' pigrizia: il ripiego si negozia all'apertura, e la
 * sostituzione del costruttore WebSocket deve avvenire prima che sockjs-client
 * venga importato. Vedi il commento dentro index.html.
 */
export function impostaModalitaWs(modalita) {
  if (modalita === 'normale') sessionStorage.removeItem(CHIAVE_WS)
  else sessionStorage.setItem(CHIAVE_WS, modalita)
  window.location.reload()
}

export function leggiTrasportoScelto() {
  const valore = sessionStorage.getItem(CHIAVE_TRASPORTO)
  return valore === 'websocket' || valore === 'xhr-streaming' || valore === 'xhr-polling'
    ? valore
    : 'auto'
}

export function salvaTrasportoScelto(trasporto) {
  if (trasporto === 'auto') sessionStorage.removeItem(CHIAVE_TRASPORTO)
  else sessionStorage.setItem(CHIAVE_TRASPORTO, trasporto)
}

/**
 * Il terzo parametro del costruttore di SockJS.
 *
 * Se la scelta e' 'auto' restituiamo undefined, cioe' non passiamo nessuna opzione:
 * SockJS prova i trasporti nel suo ordine, con il WebSocket per primo.
 */
export function opzioniSockJs(trasporto) {
  return trasporto === 'auto' ? undefined : { transports: [trasporto] }
}

/**
 * Vero solo se lo script di index.html ha davvero sostituito il costruttore WebSocket.
 *
 * Serve a non raccontare a lezione una modifica che non e' attiva: se l'utente ha
 * premuto il tasto ma la pagina non si e' ricaricata, questa restituisce false
 * e il pannello lo dice.
 */
export function webSocketManomesso() {
  return 'sostituito' in window.WebSocket
}
