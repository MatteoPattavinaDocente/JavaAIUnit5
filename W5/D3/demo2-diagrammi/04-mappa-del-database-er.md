# Demo 2 · Mappa del database

## Prompt

Leggi tutte le entità in `be/src/main/java/it/epicode/salone/model` (`Utente`, `Auto`, `Avviso`, `Preferito`, `PrezzoStorico`, `TokenRevocato`) e la configurazione JPA in `application.yml`.

Produci un `erDiagram` Mermaid con:
- tutte le tabelle con il **nome reale** (`@Table`, altrimenti quello di default), le colonne con tipo e i marcatori `PK`, `FK`, `UK`;
- le relazioni con la cardinalità corretta e un'etichetta verbale;
- i vincoli di unicità composti (per esempio la coppia `utente_id`, `auto_id` di `avvisi`).

Poi una tabella di controllo: per ogni tabella, entità Java, file e riga della dichiarazione.

Infine una sezione "Da verificare sul database vero": schema generato da `ddl-auto: update`, quindi confronta con `\d nome_tabella` su PostgreSQL e segnala le differenze possibili (indici mancanti, colonne rimaste da versioni precedenti).

## Regole

- Nessuna tabella o colonna che non derivi da un'entità o da un `@Column`.
- I campi calcolati o transitori non compaiono.
- Le relazioni `LAZY` ed `EAGER` si annotano in un commento, non cambiano il diagramma.
