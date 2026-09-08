# React + Vite (versione JSX)

Questa cartella e' la stessa applicazione di `FE`, riscritta in JavaScript puro:
i file sono `.js` e `.jsx` invece di `.ts` e `.tsx`, non c'e' TypeScript fra le
dipendenze e non ci sono i `tsconfig`. Il codice, i nomi e i commenti sono gli
stessi, cosi' si possono mettere i due progetti uno accanto all'altro e vedere
esattamente cosa aggiunge TypeScript e cosa invece resta identico.

## Cosa cambia rispetto alla versione TSX

Le annotazioni di tipo spariscono: `const [pagina, setPagina] = useState(null)`
al posto di `useState<Pagina | null>(null)`, e le props dei componenti si
prendono con la destrutturazione senza dichiararne la forma. Al posto dei tipi
esportati (`type NotificationDto = ...`) restano dei commenti che descrivono la
forma dei dati: il browser non li controlla, quindi tocca a noi ricordarcene.

Lo script di build non chiama piu' il compilatore: `vite build` e basta, senza
`tsc -b` davanti. Non essendoci un compilatore, un errore come `pagina.contenuto`
scritto al posto di `pagina.content` non viene segnalato da nessuno: si scopre
solo aprendo la pagina e leggendo `undefined`.

## Comandi

```
npm install
npm run dev      # server di sviluppo su http://localhost:5173
npm run build    # build di produzione in dist/
npm run lint     # oxlint
```

Il backend resta quello della cartella `BE`, identico: la differenza sta solo
nel frontend.

## Una alla volta

Le due cartelle `FE` e `FEJSX` usano la stessa porta 5173, che e' anche l'unica
che il backend autorizza in CORS: si tiene aperto un `npm run dev` per volta.
