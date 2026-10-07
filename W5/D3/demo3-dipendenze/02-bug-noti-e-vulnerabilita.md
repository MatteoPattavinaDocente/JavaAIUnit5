# Demo 3 · Bug noti e vulnerabilità, dipendenza per dipendenza

Da usare subito dopo il prompt 1, nella stessa conversazione: la tabella dell'elenco è l'input.

## Prompt 2 · Una scheda per ogni dipendenza

Prendi la tabella del passo precedente. Per **ogni riga**, una alla volta e senza saltarne nessuna, compila questa scheda:

1. **Bug noti**: problemi e vulnerabilità pubblicati per la **versione risolta** di Salone (identificativo CVE o GHSA, gravità, versioni interessate, versione che li corregge). Se non ce ne sono che tu conosca, scrivi "nessuno noto a me".
2. **Salone è esposto?** Cerca nel codice come la dipendenza viene usata (`file:riga`) e decidi se la parte difettosa è raggiungibile: la funzionalità coinvolta è usata, con input che arrivano da fuori?
3. **Verdetto**, uno solo fra:
   - `VULNERABILE`: versione interessata **e** parte difettosa usata;
   - `NON RAGGIUNGIBILE`: versione interessata ma la parte difettosa non è usata;
   - `NON INTERESSATO`: la versione di Salone è già corretta;
   - `NON VERIFICABILE`: non hai abbastanza informazioni.
4. **Che cosa fare**: aggiornare a quale versione, oppure la modifica di configurazione, oppure "niente".

Chiudi con una tabella riassuntiva ordinata per gravità: dipendenza · verdetto · azione.

## Onestà sui dati

Non hai una base dati di vulnerabilità aggiornata a oggi, a meno che tu non possa interrogarne una. Quindi:

- dichiara all'inizio la data fino a cui arriva la tua conoscenza;
- **non inventare** identificativi CVE: se non sei certo di un numero, descrivi il problema senza numero e segnalalo come da verificare;
- per ogni ambito indica il comando che dà la risposta autorevole:
  - backend: `./mvnw -f be/pom.xml org.owasp:dependency-check-maven:check`
  - frontend: `npm audit` dentro `fe/`
  - tutto il repository: `osv-scanner scan source .`

## Regole

- Ogni affermazione sul codice di Salone porta `file:riga`.
- Un verdetto `VULNERABILE` senza il punto del codice che usa la parte difettosa non è ammesso.
- Sola lettura: non modificare nessun file.
- Salva tutto in un file .md in /docs e se riesci crea dei diagrammi mermaid esplicativi