# FEJSX — gemella in JavaScript puro

Stessa applicazione della cartella `FE`, scritta in JSX invece di TSX: nessun
TypeScript, nessun `tsconfig`, nessun pacchetto `@types/*`.

## Avvio

```bash
npm install
cp .env.example .env   # inserire VITE_GOOGLE_MAPS_API_KEY
npm run dev
```

Le due versioni girano una alla volta (Vite occupa la stessa porta 5173): per
tenerle aperte insieme, avviarne una con `npm run dev -- --port 5174`.

## Cosa cambia rispetto a FE (TSX)

| FE (TypeScript) | FEJSX (JavaScript) |
| --- | --- |
| `.tsx` / `.ts` | `.jsx` / `.js` |
| `type Props = { ... }` | prop solo destrutturate, contratto nei commenti |
| `useState<T \| null>(null)` | `useState(null)` |
| `getElementById('root')!` | `getElementById('root')` |
| `type` / `interface` per le risposte HTTP | `@typedef` JSDoc, senza controlli |
| `build: tsc -b && vite build` | `build: vite build` |

Gli errori che TypeScript segnalava in fase di compilazione (campo scritto male,
valore `null` usato senza controllo, stringa passata dove serve un numero) qui
arrivano solo a runtime: e' il motivo per cui il corso usa la versione TSX.
