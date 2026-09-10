import { createContext, useContext } from 'react'
import type { Notifica } from '../api/types'

export type AscoltatoreNotifica = (notifica: Notifica) => void

export interface StompContextValue {
  connesso: boolean
  /** Registra un ascoltatore sulle notifiche in arrivo; restituisce la funzione di annullamento. */
  onNotifica: (ascoltatore: AscoltatoreNotifica) => () => void
}

export const StompContext = createContext<StompContextValue | null>(null)

export function useStomp() {
  const ctx = useContext(StompContext)
  if (!ctx) throw new Error('useStomp va usato dentro StompProvider')
  return ctx
}
