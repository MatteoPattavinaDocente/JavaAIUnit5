// Nella versione TypeScript questo file si chiamava types.ts: conteneva le costanti
// piu' le interface (Report, Viewport, CreateReportPayload, GeocodeResult). Qui le
// interface non esistono: restano le costanti, e le forme dei dati sono descritte
// come typedef JSDoc, utili all'editor ma senza alcun controllo a compile time.

export const CATEGORIES = ['BUCA', 'ILLUMINAZIONE', 'RIFIUTI', 'ALTRO']

/** Colore del marker per categoria: unica fonte di verita', usata anche dalla legenda. */
export const CATEGORY_COLOR = {
  BUCA: '#d93025',
  ILLUMINAZIONE: '#f9ab00',
  RIFIUTI: '#1e8e3e',
  ALTRO: '#5f6368',
}

export const CATEGORY_LABEL = {
  BUCA: 'Buca',
  ILLUMINAZIONE: 'Illuminazione',
  RIFIUTI: 'Rifiuti',
  ALTRO: 'Altro',
}

/**
 * Una delle stringhe in CATEGORIES. In TypeScript era un union type derivato
 * dall'array ('BUCA' | 'ILLUMINAZIONE' | ...): scrivere 'BUCHE' era un errore di
 * compilazione. Qui passa, e il backend risponde 400.
 * @typedef {'BUCA'|'ILLUMINAZIONE'|'RIFIUTI'|'ALTRO'} Category
 */

/**
 * @typedef {object} Report
 * @property {number} id
 * @property {Category} category
 * @property {string} description
 * @property {number} latitude
 * @property {number} longitude
 * @property {string|null} address
 * @property {string} createdAt
 */

/**
 * Il riquadro visibile della mappa: angolo sud-ovest e angolo nord-est.
 * @typedef {object} Viewport
 * @property {number} swLat
 * @property {number} swLng
 * @property {number} neLat
 * @property {number} neLng
 */

/**
 * @typedef {object} CreateReportPayload
 * @property {Category} category
 * @property {string} description
 * @property {number} latitude
 * @property {number} longitude
 * @property {string|null} [address]
 */

/**
 * @typedef {object} GeocodeResult
 * @property {number} latitude
 * @property {number} longitude
 * @property {string} formattedAddress
 */
