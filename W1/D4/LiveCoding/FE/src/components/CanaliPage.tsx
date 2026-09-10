import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError, api } from '../api/client'
import type { Canale } from '../api/types'
import { useAuth } from '../auth/authContext'

type Vista = 'tutti' | 'iscritto'

function messaggioErrore(err: unknown, ripiego: string) {
  return err instanceof ApiError ? [err.message, ...err.dettagli].join(' — ') : ripiego
}

interface Props {
  onApriCanale: (canale: Canale) => void
  /** Cambia quando le iscrizioni sono state modificate altrove (es. dalla pagina del canale). */
  versioneEsterna: number
}

export function CanaliPage({ onApriCanale, versioneEsterna }: Props) {
  const { sessione } = useAuth()
  const [vista, setVista] = useState<Vista>('tutti')
  const [pagina, setPagina] = useState(0)
  /** Incrementato dopo ogni modifica: e' il segnale di ricarica per l'effect. */
  const [versione, setVersione] = useState(0)

  const [canali, setCanali] = useState<Canale[]>([])
  const [idIscritti, setIdIscritti] = useState<Set<string>>(new Set())
  const [totalePagine, setTotalePagine] = useState(1)
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState<string | null>(null)

  const [nome, setNome] = useState('')
  const [descrizione, setDescrizione] = useState('')
  const [creazioneAperta, setCreazioneAperta] = useState(false)

  useEffect(() => {
    let annullato = false

    async function carica() {
      try {
        const [elenco, iscritti] = await Promise.all([
          vista === 'tutti' ? api.canali(pagina) : api.canaliIscritto(pagina),
          // serve sempre: decide se mostrare Segui o Smetti di seguire
          api.canaliIscritto(0),
        ])
        if (annullato) return
        setCanali(elenco.content)
        setTotalePagine(Math.max(elenco.totalPages, 1))
        setIdIscritti(new Set(iscritti.content.map((c) => c.id)))
        setErrore(null)
      } catch (err) {
        if (!annullato) setErrore(messaggioErrore(err, 'Backend non raggiungibile'))
      } finally {
        if (!annullato) setCaricamento(false)
      }
    }

    void carica()
    return () => {
      annullato = true
    }
  }, [vista, pagina, versione, versioneEsterna])

  async function cambiaIscrizione(canale: Canale) {
    try {
      if (idIscritti.has(canale.id)) await api.unfollow(canale.id)
      else await api.follow(canale.id)
      setVersione((v) => v + 1)
    } catch (err) {
      setErrore(messaggioErrore(err, 'Operazione non riuscita'))
    }
  }

  async function creaCanale(e: FormEvent) {
    e.preventDefault()
    try {
      await api.creaCanale(nome, descrizione)
      setNome('')
      setDescrizione('')
      setCreazioneAperta(false)
      setPagina(0)
      setVersione((v) => v + 1)
    } catch (err) {
      setErrore(messaggioErrore(err, 'Creazione non riuscita'))
    }
  }

  function cambiaVista(nuova: Vista) {
    setVista(nuova)
    setPagina(0)
    setCaricamento(true)
  }

  return (
    <section className="colonna">
      <div className="intestazione-sezione">
        <div className="tab-gruppo">
          <button
            className={vista === 'tutti' ? 'tab attiva' : 'tab'}
            onClick={() => cambiaVista('tutti')}
          >
            Tutti i canali
          </button>
          <button
            className={vista === 'iscritto' ? 'tab attiva' : 'tab'}
            onClick={() => cambiaVista('iscritto')}
          >
            Seguiti
          </button>
        </div>
        <button className="bottone primario" onClick={() => setCreazioneAperta((v) => !v)}>
          {creazioneAperta ? 'Annulla' : '+ Nuovo canale'}
        </button>
      </div>

      {creazioneAperta && (
        <form className="card riquadro-creazione" onSubmit={creaCanale}>
          <label className="campo">
            <span>Nome</span>
            <input value={nome} onChange={(e) => setNome(e.target.value)} placeholder="Annunci" required />
          </label>
          <label className="campo">
            <span>Descrizione</span>
            <textarea
              value={descrizione}
              onChange={(e) => setDescrizione(e.target.value)}
              placeholder="A cosa serve questo canale"
              rows={2}
            />
          </label>
          <button className="bottone primario">Crea canale</button>
        </form>
      )}

      {errore && <p className="avviso errore">{errore}</p>}

      {caricamento ? (
        <p className="vuoto">Caricamento…</p>
      ) : canali.length === 0 ? (
        <p className="vuoto">
          {vista === 'iscritto' ? 'Non segui ancora nessun canale.' : 'Nessun canale: creane uno.'}
        </p>
      ) : (
        <ul className="lista-canali">
          {canali.map((canale) => {
            const iscritto = idIscritti.has(canale.id)
            const proprietario = canale.idUtente === sessione?.id
            return (
              <li key={canale.id} className="card riga-canale">
                <button className="riga-canale-testo" onClick={() => onApriCanale(canale)}>
                  <h3>
                    {canale.nome}
                    {proprietario && <span className="etichetta tipo-personal">Tuo canale</span>}
                  </h3>
                  <p>{canale.descrizione || <em>Nessuna descrizione</em>}</p>
                </button>
                <div className="riga-canale-azioni">
                  <button className="bottone piccolo" onClick={() => onApriCanale(canale)}>
                    Apri
                  </button>
                  {/* il proprietario non puo' iscriversi al proprio canale */}
                  {!proprietario && (
                    <button
                      className={iscritto ? 'bottone piccolo attenuato' : 'bottone piccolo primario'}
                      onClick={() => void cambiaIscrizione(canale)}
                    >
                      {iscritto ? 'Smetti di seguire' : 'Segui'}
                    </button>
                  )}
                </div>
              </li>
            )
          })}
        </ul>
      )}

      {totalePagine > 1 && (
        <div className="paginazione">
          <button className="bottone piccolo" disabled={pagina === 0} onClick={() => setPagina((p) => p - 1)}>
            ← Precedente
          </button>
          <span>
            Pagina {pagina + 1} di {totalePagine}
          </span>
          <button
            className="bottone piccolo"
            disabled={pagina + 1 >= totalePagine}
            onClick={() => setPagina((p) => p + 1)}
          >
            Successiva →
          </button>
        </div>
      )}
    </section>
  )
}
