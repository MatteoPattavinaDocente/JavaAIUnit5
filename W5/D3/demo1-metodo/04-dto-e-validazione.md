# Demo 1 · DTO e validazione

## Prompt

Analizza il pacchetto `be/src/main/java/it/epicode/salone/dto` del progetto Salone.

Tabella con una riga per DTO: nome, campi, vincoli di Bean Validation (`@NotBlank`, `@Size`, `@Email`, ...), quale controller lo usa e se è un DTO di ingresso o di uscita.

Poi rispondi:
1. Esiste un'entità restituita direttamente al client senza passare da un DTO?
2. Che differenza c'è fra `AutoAdminView` e `AutoPubblicaView`? Quali campi vede solo l'amministratore?
3. I DTO di ingresso possono impostare campi che devono restare decisi dal server (ruolo, prezzo, proprietario)? Spiega perché sì o perché no.

## Regole

- Cita sempre file e riga. Un'affermazione senza riferimento va tolta.
- Se non sei sicuro di qualcosa, scrivi "non verificabile" invece di indovinare.
- Non inventare DTO o campi: se non li trovi nel codice, non esistono.
- Sola lettura: non modificare nessun file.
