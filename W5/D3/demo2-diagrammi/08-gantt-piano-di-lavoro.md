# Demo 2 · Gantt: il piano di lavoro dopo il reverse engineering

Salone non ha una cronologia Git (la cartella non è un repository), quindi il Gantt non racconta il passato: pianifica il lavoro **da fare**.

## Prompt

Sulla base di quello che hai ricostruito di Salone, produci un `gantt` Mermaid del piano di lavoro per portare il progetto da "ereditato" a "sotto controllo". Usa `dateFormat YYYY-MM-DD`, parti dal lunedì successivo a oggi e usa giorni lavorativi (`excludes weekends`).

Sezioni e attività, ciascuna con una stima in giorni e le dipendenze reali fra attività (`after`):

- **Comprensione**: mappa dei moduli, percorso di una richiesta, schema del database.
- **Rete di sicurezza**: test di architettura che fissano la regola dei livelli, test sulle regole di business degli avvisi, eliminazione dei due cicli fra pacchetti (`service`↔`event`, `config`↔`security`).
- **Dipendenze**: aggiornamenti emersi dal controllo di vulnerabilità, con priorità per le gravi.
- **API**: specifica OpenAPI generata dal codice, collezione Postman, pagina Scalar.
- **Documentazione**: diagrammi nel repository, aggiornamento di `MAPPA.md`.

Marca le attività critiche (`crit`) e una pietra miliare (`milestone`) al termine di ogni sezione.

Sotto il diagramma scrivi le **ipotesi delle stime** e chiedimi conferma sulle tre più incerte prima di considerarle valide.

## Regole

- Le attività devono derivare da problemi trovati nel codice o nelle dipendenze, non da un elenco generico di buone pratiche.
- Non inventare durate precise: se una stima è un'ipotesi, dillo.
- Crea in nella cartella docs/ un file md con i diagrammi generati