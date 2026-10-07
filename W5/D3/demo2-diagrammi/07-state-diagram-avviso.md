# Demo 2 · State diagram: il ciclo di vita di un avviso

## Prompt

Leggi `model/Avviso`, `service/AvvisoService`, `service/InvioAvvisi`, `repository/AvvisoRepository` e `web/AvvisiController`.

Produci uno `stateDiagram-v2` Mermaid dello stato di un avviso di prezzo. Gli stati derivano dai campi `attivo`, `inviato` e `inviatoIl`: individua tu le combinazioni che esistono davvero (per esempio: armato, inviato, disattivato) e dai a ciascuna un nome.

Per ogni transizione scrivi l'evento che la provoca e il metodo che la esegue: creazione, prezzo che attraversa la soglia, modifica della soglia (una soglia nuova "riarma" l'avviso), disattivazione dal link della mail, eliminazione.

Sotto il diagramma, una tabella "transizione → metodo → file:riga" e l'elenco delle **combinazioni impossibili** dei tre campi che il codice non impedisce.

## Regole

- Nessuno stato che non corrisponda a valori raggiungibili dei campi.
- Se una transizione ti sembra plausibile ma non la trovi nel codice, non disegnarla.
- Crea in nella cartella docs/ un file md con i diagrammi generati