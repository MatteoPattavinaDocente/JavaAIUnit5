# Demo 1 · Controller

Progetto: Salone, cartella `be/` (Spring Boot 4.1.1, Java 25). Da incollare nell'agente, con la radice del progetto come cartella di lavoro.

## Prompt

Sono un ingegnere che eredita il progetto Salone e non lo conosce. Analizza **solo** il pacchetto `be/src/main/java/it/epicode/salone/web`.

Per ogni controller dammi: nome della classe, prefisso di `@RequestMapping` e sicurezza a livello di classe. Poi una tabella con una riga per metodo:

| verbo | percorso completo | DTO in ingresso | tipo restituito | codice di stato | annotazione di sicurezza | servizio chiamato |

Alla fine rispondi a tre domande:
1. Esiste un controller che parla direttamente con un repository, saltando il servizio?
2. Quali eccezioni gestisce `ApiExceptionHandler` e con quale codice HTTP risponde a ciascuna?
3. Quali metodi non hanno nessuna annotazione di sicurezza e su quale regola della catena si appoggiano?

## Regole

- Cita sempre file e riga, per esempio `AuthController.java:34`. Un'affermazione senza riferimento va tolta.
- Se non sei sicuro di qualcosa, scrivi "non verificabile" invece di indovinare.
- Non inventare classi, metodi o endpoint: se non li trovi nel codice, non esistono.
- Sola lettura: non modificare nessun file.
- Crea in nella cartella docs/ un file md con il resoconto di quello che hai visto.
