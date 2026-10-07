# Demo 1 · Repository e model

## Prompt

Analizza i pacchetti `be/src/main/java/it/epicode/salone/model` e `be/src/main/java/it/epicode/salone/repository` del progetto Salone.

Per ogni entità dammi: tabella, campi chiave, relazioni con il tipo di fetch (LAZY o EAGER) e vincoli di unicità. Per ogni repository elenca i metodi derivati e le query scritte a mano (`@Query`), con una riga su a che cosa serve ciascuna.

Approfondisci due punti:
- `AvvisoRepository.prenotaInvio` e `AvvisoRepository.attraversati`: che cosa fanno e perché sono scritte così.
- Le relazioni EAGER: dove possono produrre un problema N+1 o caricare più dati del necessario.

## Regole

- Cita sempre file e riga. Un'affermazione senza riferimento va tolta.
- Se non sei sicuro di qualcosa, scrivi "non verificabile" invece di indovinare.
- Non inventare entità, campi o query: se non le trovi nel codice, non esistono.
- Sola lettura: non modificare nessun file.
