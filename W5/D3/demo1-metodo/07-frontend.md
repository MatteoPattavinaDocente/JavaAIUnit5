# Demo 1 · Frontend

## Prompt

Analizza la cartella `fe/src` del progetto Salone (React 19, Vite, TypeScript).

Dammi: l'elenco delle pagine in `pagine/` con una riga ciascuna; come funziona il router (`lib/router.ts`); dove viene salvato il token e per quanto tempo (`lib/sessione.ts`); e la tabella degli endpoint del backend chiamati da `lib/api.ts` (verbo e percorso).

Confronta quella tabella con `MAPPA.md`: quali endpoint del backend il frontend non usa mai? Quali chiamate del frontend non corrispondono a nessun endpoint?

## Regole

- Cita sempre file e riga. Un'affermazione senza riferimento va tolta.
- Se non sei sicuro di qualcosa, scrivi "non verificabile" invece di indovinare.
- Non inventare pagine o chiamate: se non le trovi nel codice, non esistono.
- Sola lettura: non modificare nessun file.
