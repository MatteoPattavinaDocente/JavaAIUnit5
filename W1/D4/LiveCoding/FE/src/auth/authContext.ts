import { createContext, useContext } from 'react'

export interface Sessione {
  id: string
  username: string
  token: string
}

export interface AuthContextValue {
  sessione: Sessione | null
  login: (username: string, password: string) => Promise<void>
  register: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

export const CHIAVE_STORAGE = 'livecoding.sessione'

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth va usato dentro AuthProvider')
  return ctx
}
