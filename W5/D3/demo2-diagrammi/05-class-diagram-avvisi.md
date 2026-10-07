# Demo 2 · Class diagram: la funzionalità degli avvisi di prezzo

## Prompt

Limita l'analisi a **una sola funzionalità**: gli avvisi di prezzo. Le classi coinvolte sono `AvvisiController`, `AvvisoService`, `AvvisoRepository`, `Avviso`, i DTO `AvvisoNuovo`, `AvvisoModifica`, `AvvisoView`, `DisattivazioneRichiesta`, e per la parte mail `AvvisiListener`, `InvioAvvisi`, `MailAvviso`, `Postino`, `PrezzoCambiato`.

Produci un `classDiagram` Mermaid con:
- per ogni classe solo i membri utili a capire la funzionalità (non tutti i campi);
- le relazioni con la freccia giusta: `-->` dipendenza, `*--` composizione, `..>` uso, `<|--` ereditarietà o implementazione;
- le annotazioni importanti come stereotipi (`<<Controller>>`, `<<Service>>`, `<<Repository>>`, `<<Entity>>`, `<<record>>`).

Tieni il diagramma **sotto i venti nodi**. Poi una tabella di controllo "freccia → campo, parametro o chiamata che la giustifica, con file:riga".

## Regole

- Ogni freccia deve corrispondere a un campo, un parametro del costruttore o una chiamata reale.
- Non usare la freccia di ereditarietà per una semplice dipendenza.
- Se un tipo non lo trovi, non disegnarlo.
