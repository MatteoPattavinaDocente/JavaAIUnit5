# Demo 1 · Config e sicurezza

## Prompt

Analizza i pacchetti `be/src/main/java/it/epicode/salone/config` e `be/src/main/java/it/epicode/salone/security`, più `be/src/main/resources/application.yml`.

Ricostruisci:
1. La catena dei filtri di `SicurezzaConfig`: regole per metodo e percorso, nell'ordine in cui vengono valutate, e che cosa fa la regola finale.
2. Il JWT: algoritmo, chi firma, quali validatori dei claim sono attivi, come funziona la revoca (`RevocaValidator`, `TokenRevocato`).
3. `LimiteFrequenzaFiltro`: su quali percorsi agisce, il limite, come identifica il client.
4. Il CORS: quali origini ammette e da quale variabile d'ambiente arrivano.
5. Ogni valore letto dall'ambiente (`${...}`): nome della variabile, valore di ripiego, e se il ripiego è un segreto.

Chiudi con l'elenco dei segreti che **non** devono stare nel repository e di quelli che ci stanno per errore.

## Regole

- Cita sempre file e riga. Un'affermazione senza riferimento va tolta.
- Se non sei sicuro di qualcosa, scrivi "non verificabile" invece di indovinare.
- Non riportare mai il valore di un segreto: indica solo dove sta.
- Sola lettura: non modificare nessun file.
