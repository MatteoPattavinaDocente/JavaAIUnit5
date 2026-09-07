# FEJSX — gemella in JavaScript puro

Stesso frontend della cartella `FE` (segnalazioni urbane su mappa), scritto in JSX
invece di TSX: nessun TypeScript, nessun `tsconfig`, nessun pacchetto `@types/*`.
Il backend e' lo stesso: `Project/BE` su `http://localhost:8080`.

## Avvio

```bash
npm install
cp .env.example .env   # inserire VITE_GOOGLE_MAPS_API_KEY
npm run dev
```

Gira sulla porta **5174** (`FE` usa la 5173), quindi le due versioni possono
stare aperte insieme. Il proxy `/api` punta comunque a `localhost:8080`.

## Cosa cambia rispetto a FE (TSX)

| FE (TypeScript) | FEJSX (JavaScript) |
| --- | --- |
| `src/types.ts` (costanti + `interface`) | `src/constants.js` (solo costanti, forme come `@typedef`) |
| `interface Props { ... }` | prop solo destrutturate, contratto nei commenti |
| `useState<Report[]>([])` | `useState([])` |
| `parse<T>(response)` con `as T` | `parse(response)` |
| `event.target.value as Category` | `event.target.value` |
| `useRef<google.maps.Map \| null>(null)` | `useRef(null)` |
| `build: tsc -b && vite build` | `build: vite build` |

Gli errori che TypeScript bloccava in compilazione (categoria inesistente,
coordinata `null` letta senza controllo, campo rinominato nel backend) qui
compaiono solo a runtime: e' il motivo per cui il progetto di riferimento e' in TSX.
