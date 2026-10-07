# Demo 1 · Evento e mail

## Prompt

Ricostruisci il percorso completo di un avviso di prezzo in Salone, dal cambio di prezzo all'email. Leggi `AdminAutoController.cambiaPrezzo`, `AutoService`, il pacchetto `event`, `InvioAvvisi`, `MailAvviso`, `Postino` e il template `be/src/main/resources/templates/email/avviso-prezzo.html`.

Voglio una lista numerata di passi, uno per riga, con il metodo che lo esegue. Poi spiega:
1. Perché il listener usa `AFTER_COMMIT` e che cosa succederebbe con un `@EventListener` normale.
2. Perché `@Async` e su quale tipo di thread gira.
3. Come si evita di inviare due mail per lo stesso avviso (`prenotaInvio`).
4. Come funziona il token di disattivazione: che cosa sta nel database e che cosa nel link.
5. Che cosa succede se l'invio fallisce per un avviso: gli altri partono?

## Regole

- Cita sempre file e riga. Un'affermazione senza riferimento va tolta.
- Se non sei sicuro di qualcosa, scrivi "non verificabile" invece di indovinare.
- Non inventare passi: ogni passo deve corrispondere a una chiamata che esiste nel codice.
- Sola lettura: non modificare nessun file.
