# Demo 3 · Le dipendenze di Salone

## Prompt 1 · L'elenco completo

Elenca **tutte** le dipendenze del progetto Salone, dichiarate e non. Guarda:

- `be/pom.xml`: il parent (Spring Boot), le dipendenze dirette con scope, la versione di Java.
- `fe/package.json` e `fe/package-lock.json`: `dependencies` e `devDependencies`.
- `be/Dockerfile`: le immagini di base.
- `.github/workflows/ci.yml`: le azioni usate e le loro versioni.
- I servizi esterni chiamati dal codice (cerca gli host negli URL: Wikipedia, jsDelivr, NHTSA e ogni altro).

Restituisci una tabella con le colonne: **ambito** (be, fe, docker, ci, servizio esterno) · **nome** · **versione dichiarata** · **versione effettivamente risolta** · **scope** (compile, runtime, test, dev) · **a che cosa serve in Salone** (una riga, con il file che la usa).

Per il backend, se puoi eseguire comandi, usa `./mvnw -f be/pom.xml dependency:tree`; per il frontend `npm ls --all` dentro `fe/`. Se non puoi eseguirli, dillo e lavora dai file.

Controllo finale: dichiara quante dipendenze dirette hai trovato per ambito. Nel `pom.xml` sono 13, in `package.json` sono 16.

## Regole

- Cita sempre il file da cui ricavi ogni riga.
- Se una versione non è dichiarata (la decide il parent o il lock file), scrivilo e indica chi la decide.
- Non aggiungere dipendenze che non vedi nei file.
- Sola lettura: non modificare nessun file.
- Salva tutto in un file .md in /docs e se riesci crea dei diagrammi mermaid esplicativi
