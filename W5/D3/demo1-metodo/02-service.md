# Demo 1 · Service

## Prompt

Analizza **solo** il pacchetto `be/src/main/java/it/epicode/salone/service` del progetto Salone.

Per ogni classe scrivi, in una riga ciascuno: la responsabilità, le dipendenze iniettate dal costruttore, i metodi pubblici e dove compare `@Transactional`.

Poi ricostruisci le **regole di business** che il codice fa rispettare, per esempio i limiti sulle soglie di prezzo, l'unicità di un avviso per utente e auto, che cosa succede a un utente ADMIN che prova a cancellarsi. Per ogni regola indica in quale metodo vive.

Infine segnala: i servizi con più di quattro dipendenze, le eccezioni personalizzate e dove vengono lanciate, i metodi che scrivono sul database senza `@Transactional`.

## Regole

- Cita sempre file e riga. Un'affermazione senza riferimento va tolta.
- Se non sei sicuro di qualcosa, scrivi "non verificabile" invece di indovinare.
- Non inventare classi, metodi o regole: se non le trovi nel codice, non esistono.
- Sola lettura: non modificare nessun file.
- Crea in nella cartella docs/ un file md con il resoconto di quello che hai visto.