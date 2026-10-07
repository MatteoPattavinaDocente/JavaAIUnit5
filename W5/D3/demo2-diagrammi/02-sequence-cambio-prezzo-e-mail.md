# Demo 2 · Sequence diagram: cambio di prezzo e mail di avviso

## Prompt

Leggi `AdminAutoController.cambiaPrezzo`, `AutoService`, `PrezzoCambiato`, `AvvisiListener`, `InvioAvvisi`, `AvvisoRepository`, `MailAvviso` e `Postino`.

Produci un `sequenceDiagram` Mermaid del flusso `PUT /api/admin/auto/{id}/prezzo` fino alla mail all'utente. Deve mostrare in modo visibile:

- la transazione che salva il nuovo prezzo e la riga di `PrezzoStorico`;
- la pubblicazione dell'evento `PrezzoCambiato`;
- che il listener parte **dopo il commit** (`AFTER_COMMIT`);
- che il listener gira su un altro thread (`@Async`): la risposta all'amministratore esce **prima** delle mail (usa `par` o una nota);
- il ciclo sugli avvisi attraversati dal prezzo (`loop`) e l'UPDATE condizionale `prenotaInvio`;
- il caso in cui l'invio di un avviso fallisce e il ciclo continua.

Sotto il diagramma, una tabella "messaggio → file:riga".

## Regole

- Ogni messaggio corrisponde a una chiamata che esiste. Non disegnare passaggi "logici" che nel codice non ci sono.
- Le note (`Note over`) servono a spiegare `AFTER_COMMIT` e `@Async`, non a riempire.
