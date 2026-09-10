import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { api, impostaHandlerNonAutorizzato, impostaToken } from '../api/client'
import { AuthContext, CHIAVE_STORAGE } from './authContext'
import type { AuthContextValue, Sessione } from './authContext'

function leggiSessioneSalvata(): Sessione | null {
  try {
    const grezzo = localStorage.getItem(CHIAVE_STORAGE)
    return grezzo ? (JSON.parse(grezzo) as Sessione) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessione, setSessione] = useState<Sessione | null>(leggiSessioneSalvata)

  // Il client HTTP non conosce React: il token va allineato durante il render,
  // perche' gli effect dei figli partono prima di quelli del provider.
  impostaToken(sessione?.token ?? null)

  const chiudiSessione = useCallback(() => {
    impostaToken(null)
    localStorage.removeItem(CHIAVE_STORAGE)
    setSessione(null)
  }, [])

  useEffect(() => {
    // un 401 da qualunque chiamata (token scaduto o revocato) riporta al login
    impostaHandlerNonAutorizzato(chiudiSessione)
    return () => impostaHandlerNonAutorizzato(null)
  }, [chiudiSessione])

  const salva = useCallback((nuova: Sessione) => {
    impostaToken(nuova.token)
    localStorage.setItem(CHIAVE_STORAGE, JSON.stringify(nuova))
    setSessione(nuova)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      sessione,
      login: async (username, password) => salva(await api.login(username, password)),
      register: async (username, password) => salva(await api.register(username, password)),
      logout: async () => {
        try {
          await api.logout()
        } finally {
          // la sessione locale si chiude comunque, anche se la chiamata fallisce
          chiudiSessione()
        }
      },
    }),
    [sessione, salva, chiudiSessione],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
