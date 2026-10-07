# Demo 2 · Sequence diagram: registrazione e login

## Prompt

Leggi `AuthController`, `UtenteService`, `TokenService`, `UtenteRepository` e l'encoder delle password usato da `UtenteService`.

Produci **due** diagrammi Mermaid `sequenceDiagram` in un unico file `.md`, ciascuno in un blocco ```mermaid:

1. `POST /api/auth/registrazione`, dal client alla risposta: mostra il caso di email già registrata (`ConflittoException`) e il caso riuscito, in cui la risposta contiene già il token (`accessoRiuscito`).
2. `POST /api/auth/login`, incluso il caso di credenziali errate: mostra che l'errore è lo stesso per email inesistente e password sbagliata (`CredenzialiNonValideException`).

Requisiti:
- Un partecipante per ogni classe realmente coinvolta, con il nome esatto della classe.
- Ogni messaggio è una chiamata di metodo che esiste, con il nome del metodo.
- Le risposte con freccia tratteggiata (`-->>`), le chiamate con freccia piena (`->>`).
- Il blocco `alt` per i casi di errore, con il codice HTTP.
- Sotto ogni diagramma, una tabella "messaggio → file:riga".

## Regole

- Se una chiamata non la trovi nel codice, non disegnarla.
- Dopo il diagramma, elenca i partecipanti che hai **escluso** per semplicità.
- Controlla la sintassi: ogni `alt` ha il suo `end`.
- Crea in nella cartella docs/ un file md con i diagrammi generati