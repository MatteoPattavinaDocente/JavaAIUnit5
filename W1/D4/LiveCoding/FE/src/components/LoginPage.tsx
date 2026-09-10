import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/authContext'

export function LoginPage() {
  const { login, register } = useAuth()
  const [modo, setModo] = useState<'login' | 'register'>('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errore, setErrore] = useState<string | null>(null)
  const [inCorso, setInCorso] = useState(false)

  async function invia(e: FormEvent) {
    e.preventDefault()
    setErrore(null)
    setInCorso(true)
    try {
      if (modo === 'login') await login(username, password)
      else await register(username, password)
    } catch (err) {
      const messaggio =
        err instanceof ApiError
          ? [err.message, ...err.dettagli].join(' — ')
          : 'Backend non raggiungibile'
      setErrore(messaggio)
    } finally {
      setInCorso(false)
    }
  }

  return (
    <div className="schermata-login">
      <form className="card card-login" onSubmit={invia}>
        <h1 className="titolo-app">
          <span className="pallino" /> LiveCoding
        </h1>
        <p className="sottotitolo">Canali e notifiche in tempo reale</p>

        <div className="tab-gruppo">
          <button
            type="button"
            className={modo === 'login' ? 'tab attiva' : 'tab'}
            onClick={() => { setModo('login'); setErrore(null) }}
          >
            Accedi
          </button>
          <button
            type="button"
            className={modo === 'register' ? 'tab attiva' : 'tab'}
            onClick={() => { setModo('register'); setErrore(null) }}
          >
            Registrati
          </button>
        </div>

        <label className="campo">
          <span>Username</span>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            placeholder="mario"
            required
          />
        </label>

        <label className="campo">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete={modo === 'login' ? 'current-password' : 'new-password'}
            placeholder="••••••••"
            required
          />
        </label>

        {errore && <p className="avviso errore">{errore}</p>}

        <button className="bottone primario largo" disabled={inCorso}>
          {inCorso ? 'Attendi…' : modo === 'login' ? 'Accedi' : 'Crea account'}
        </button>

        <p className="nota">
          Le password sono salvate in chiaro: progetto didattico, non usare credenziali vere.
        </p>
      </form>
    </div>
  )
}
