import { useState } from 'react'

// Un topic a cui non siamo sottoscritti, per far vedere il rifiuto del server.
const TOPIC_VIETATO = 'vietato'

// In TSX il pannello dichiarava le sue quattordici props una per una, importando i
// tipi Errore, ModoRicezione e Scoperta da useNotifiche:
//
//   modoRicezione         'stomp' | 'polling'
//   scoperta              { titolo, ritardoMs, modo } oppure null
//   errore                { messaggio, destinazione, ora } oppure null
//   onInviaHttpSenzaPush  (a, testo) => Promise
//   onInviaHttpConPush    (a, testo) => Promise
//   onInviaViaStomp       (a, testo) => void
//   onInviaSuTopic        (topic, testo) => void
//   onControlla           () => Promise
//
// Senza TypeScript quell'elenco non serve a far girare niente, ma serve a chi legge:
// per questo resta come commento.

/**
 * Il pannello del confronto: lo stesso messaggio mandato in quattro modi diversi,
 * piu' il cronometro e il contatore delle richieste HTTP.
 *
 * Questo componente non parla ne' con il backend ne' con il broker: riceve funzioni
 * e numeri dall'hook e li mostra. Tutto il testo che si legge in pagina e' materiale
 * di lezione, scritto per essere proiettato: la spiegazione sta accanto al pulsante
 * che la dimostra.
 *
 * Da provare in aula: due finestre affiancate, una per mario e una per lucia,
 * e si guarda l'altra mentre si premono i tre pulsanti.
 */
export function PannelloConfronto({
  utente,
  utenti,
  topicSottoscritti,
  modoRicezione,
  onModoRicezione,
  richieste,
  scoperta,
  errore,
  onInviaHttpSenzaPush,
  onInviaHttpConPush,
  onInviaViaStomp,
  onInviaSuTopic,
  onControlla,
}) {
  const altri = utenti.filter((u) => u !== utente)
  const [a, setA] = useState(altri[0] ?? '')
  const [testo, setTesto] = useState('ciao')
  const [topic, setTopic] = useState('')

  // Solo i topic a cui siamo sottoscritti: sono gli unici su cui il server
  // accettera' di scrivere.
  const topicScelto = topicSottoscritti.includes(topic) ? topic : (topicSottoscritti[0] ?? '')

  // Il destinatario predefinito segue l'utente: mandare notifiche a se stessi
  // funziona, ma non fa vedere niente.
  const destinatario = altri.includes(a) ? a : (altri[0] ?? utente)

  return (
    <>
      <section className="card">
        <h2>Manda una notifica a un altro utente</h2>

        <div className="bottoni">
          <label>
            a{' '}
            <select value={destinatario} onChange={(e) => setA(e.target.value)}>
              {utenti.map((u) => (
                <option key={u} value={u}>
                  {u}
                </option>
              ))}
            </select>
          </label>
          <input
            value={testo}
            onChange={(e) => setTesto(e.target.value)}
            size={18}
            aria-label="testo del messaggio"
          />
        </div>

        <p className="nota">
          Le tre strade scrivono <strong>la stessa riga</strong> sul database, con lo stesso
          titolo. Cambia solo come parte e come arriva. Apri due finestre, una per{' '}
          {utenti.join(' e una per ')}, e guarda l altra mentre premi.
        </p>

        <ol className="strade">
          <li>
            <button type="button" onClick={() => void onInviaHttpSenzaPush(destinatario, testo)}>
              HTTP, senza push
            </button>
            <small>
              <code>POST /api/demo/messaggi/senza-push</code> &mdash; il server salva e non avvisa
              nessuno. Il destinatario non vede niente: la scoprira' al prossimo polling, o
              ricaricando. E' la notifica <em>normale</em>.
            </small>
          </li>
          <li>
            <button type="button" onClick={() => void onInviaHttpConPush(destinatario, testo)}>
              HTTP, con push STOMP
            </button>
            <small>
              <code>POST /api/demo/messaggi/con-push</code> &mdash; stessa richiesta, ma al commit
              parte l evento e <code>convertAndSendToUser</code> pubblica su{' '}
              <code>/user/{destinatario}/queue/notifications</code>. Il destinatario la vede
              subito, senza aver chiesto niente. E' il caso normale di un applicazione.
            </small>
          </li>
          <li>
            <button type="button" onClick={() => onInviaViaStomp(destinatario, testo)}>
              STOMP, frame SEND
            </button>
            <small>
              <code>SEND /app/messaggi</code> &mdash; <strong>nessuna richiesta HTTP</strong>: parte
              sul canale gia' aperto. Guarda il contatore qui sotto restare fermo, e il log dei
              frame mostrare <code>&gt;&gt;&gt; SEND</code>. Il mittente non e' nel payload: lo
              mette il server dal Principal della sessione.
            </small>
          </li>
        </ol>
      </section>

      <section className="card">
        <h2>Manda un messaggio su un topic</h2>

        {topicSottoscritti.length === 0 ? (
          <p className="avviso">
            Nessun topic sottoscritto. Vai nel pannello SUBSCRIBE, iscriviti a un topic (per
            esempio <code>/topic/ordini/42</code>), e questo elenco si popola. Senza sottoscrizione
            il server rifiuta l invio: e' la regola, non un limite dell interfaccia.
          </p>
        ) : (
          <div className="bottoni">
            <label>
              su{' '}
              <select value={topicScelto} onChange={(e) => setTopic(e.target.value)}>
                {topicSottoscritti.map((t) => (
                  <option key={t} value={t}>
                    /topic/{t}
                  </option>
                ))}
              </select>
            </label>
            <button type="button" onClick={() => onInviaSuTopic(topicScelto, testo)}>
              SEND su /topic/{topicScelto}
            </button>
          </div>
        )}

        <p className="nota">
          <code>SEND /app/topic-messaggi</code> con <code>{'{ topic, testo }'}</code>. Al server va
          il <strong>nome</strong> del topic, non la destinazione: il prefisso <code>/topic</code>{' '}
          lo mette lui, cosi' nessuno prova a scrivere su <code>/queue</code> o su{' '}
          <code>/user</code>. Il messaggio lo ricevono tutte le sessioni sottoscritte in questo
          momento, la nostra compresa, e <strong>nessun altro</strong>.
        </p>
        <p className="nota">
          Non passa dal database: <strong>un topic non ha storico</strong>. Chi non e' connesso
          adesso non lo riceve e non ha modo di recuperarlo &mdash; al contrario della coda
          personale, che e' appoggiata alla tabella <code>notifications</code>. E' la differenza
          fra un <em>topic</em> e una <em>coda</em>, e si vede: ricarica la pagina e i messaggi sui
          topic sono spariti, le notifiche personali no.
        </p>

        <div className="bottoni">
          <button type="button" onClick={() => onInviaSuTopic(TOPIC_VIETATO, testo)}>
            prova su /topic/{TOPIC_VIETATO} (non sottoscritto)
          </button>
        </div>
        <p className="nota">
          Il controllo sta <strong>sul server</strong>, in <code>SubscriptionRegistry</code>: la
          <code>select</code> qui sopra e' solo cortesia, il client puo' mandare quello che vuole.
          Questo tasto lo dimostra: il messaggio parte, e torna un rifiuto su{' '}
          <code>/user/queue/errors</code>.
        </p>

        {errore && (
          <p className="avviso">
            <strong>rifiutato dal server</strong> alle {errore.ora}
            <br />
            {errore.messaggio}
            <br />
            <small>
              Nessun frame <code>ERROR</code> di protocollo: quello chiuderebbe la sessione. Un
              messaggio su una coda personale lascia la connessione in piedi e arriva solo a chi ha
              sbagliato &mdash; guarda lo stato in testa alla pagina, e' ancora <em>connesso</em>.
            </small>
          </p>
        )}
      </section>

      <section className="card">
        <h2>Come questa pagina scopre le notifiche</h2>

        <div className="bottoni">
          <label>
            <input
              type="radio"
              name="modo"
              checked={modoRicezione === 'stomp'}
              onChange={() => onModoRicezione('stomp')}
            />
            STOMP <small>sottoscritto a /user/queue/notifications</small>
          </label>
          <label>
            <input
              type="radio"
              name="modo"
              checked={modoRicezione === 'polling'}
              onChange={() => onModoRicezione('polling')}
            />
            polling <small>una GET ogni 5s, nessuna sottoscrizione</small>
          </label>
        </div>

        <p className="riga-pill">
          <span className="pill">{richieste} richieste HTTP</span>
          <button type="button" onClick={() => void onControlla()}>
            controlla adesso
          </button>
        </p>

        {scoperta ? (
          <p className="misura">
            ultima notifica scoperta dopo <strong>{scoperta.ritardoMs} ms</strong> via{' '}
            <code>{scoperta.modo}</code>
            <br />
            <small>{scoperta.titolo}</small>
          </p>
        ) : (
          <p className="nota">nessuna notifica nuova da quando questa pagina e aperta</p>
        )}

        <p className="nota">
          Il ritardo e la distanza fra <code>createdAt</code>, cioe' il momento in cui il server
          ha scritto la riga, e il momento in cui questa pagina l ha saputo. Con STOMP sono
          decine di millisecondi e il contatore delle richieste sta fermo. Col polling il ritardo
          arriva fino a 5 secondi e il contatore cresce da solo, anche quando non succede niente:
          e' il prezzo del chiedere invece di ricevere.
        </p>
        <p className="nota">
          In modo polling la coda personale non e' sottoscritta: nel log dei frame si vede la
          UNSUBSCRIBE, e quel MESSAGE non arriva davvero. Non stiamo fingendo lato client.
        </p>
      </section>
    </>
  )
}
