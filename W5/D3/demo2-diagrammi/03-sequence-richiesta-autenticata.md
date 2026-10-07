# Demo 2 · Sequence diagram: una richiesta protetta

## Prompt

Leggi `config/SicurezzaConfig`, `config/JwtConfig`, `security/LimiteFrequenzaFiltro`, `security/RevocaValidator`, `security/Sessione`, `security/ErroriSicurezza`, `web/AvvisiController` e `service/AvvisoService`.

Produci un `sequenceDiagram` Mermaid di `GET /api/avvisi` con un token valido e, in un secondo diagramma, dello stesso endpoint con: token scaduto, token revocato, token di un altro ruolo dove serve, e limite di frequenza superato.

Mostra i filtri **nell'ordine reale** in cui la catena li esegue, e per ogni ramo di errore il codice HTTP (401, 403, 429) e chi lo produce. Nel caso valido mostra come `Sessione` ricava l'id dell'utente dal claim `sub` e come la query restringe per proprietario.

Sotto ogni diagramma, tabella "messaggio → file:riga".

## Regole

- L'ordine dei filtri va ricavato da `SicurezzaConfig`, non dall'esperienza con altri progetti.
- Se non riesci a stabilire l'ordine di due filtri, scrivilo in una nota sul diagramma.
