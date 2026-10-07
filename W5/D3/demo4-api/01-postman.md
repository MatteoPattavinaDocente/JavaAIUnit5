# Demo 4 · La mappa delle API per Postman

## Prompt

Leggi tutti i controller in `be/src/main/java/it/epicode/salone/web`, i DTO in `dto/` e la catena dei filtri in `config/SicurezzaConfig.java`.

Genera una **collection Postman v2.1** (`postman/salone.postman_collection.json`) e un **environment** (`postman/salone.postman_environment.json`).

Struttura della collection:
- una cartella per livello di accesso: `Pubblici`, `Utente`, `Admin`, con dentro una sottocartella per controller;
- una richiesta per ogni endpoint, con nome `VERBO /percorso`;
- variabili al posto dei valori fissi: `{{baseUrl}}` (locale `http://localhost:8080`), `{{token}}`, `{{autoId}}`, `{{avvisoId}}`.

Contenuto di ogni richiesta:
- corpo di esempio ricavato dal DTO, **rispettando i vincoli di validazione**;
- intestazione `Authorization: Bearer {{token}}` solo dove serve (livelli Utente e Admin);
- uno script `Tests` con almeno due controlli: lo stato atteso e la forma del corpo di risposta.

Il login (`POST /api/auth/login`) ha uno script che salva il token in `{{token}}`. Aggiungi una richiesta di prova per ogni codice di rifiuto documentato in `MAPPA.md` (401, 403, 404, 429).

Controllo finale: conta le richieste generate e confrontale con le **28** righe di `MAPPA.md`. Elenca quelle che mancano e quelle in più.

## Regole

- Nessun segreto nell'environment: `token` resta vuoto, password e chiavi non compaiono.
- Ogni richiesta porta nella descrizione `Controller.metodo` e `file:riga`.
- Se non sai il corpo di un endpoint, non inventarlo: lascia il corpo vuoto e scrivilo nella descrizione.
- La collection è un artefatto: alla prima modifica dei controller va rigenerata, non corretta a mano.
