# Demo 2 · Flowchart: architettura e deploy

## Prompt

Leggi `render.yaml`, `be/Dockerfile`, `.github/workflows/ci.yml`, `README.md` e `be/src/main/resources/application.yml`.

Produci un `flowchart LR` Mermaid dell'architettura in produzione con sottografi (`subgraph`):
- **Browser dell'utente** → **Frontend** (Static Site su Render);
- **Backend** (Web Service Docker) → **PostgreSQL** (Render);
- i servizi esterni chiamati dal backend (Wikipedia, jsDelivr per i loghi, il server SMTP) e quelli chiamati dal browser (NHTSA vPIC);
- **CI**: pull request → job `backend` e `frontend` → deploy.

Sulle frecce scrivi il protocollo (HTTPS, JDBC, SMTP). Colora in modo diverso ciò che è sotto il nostro controllo e ciò che è esterno (`classDef`).

Sotto il diagramma elenca le **variabili d'ambiente** che collegano i blocchi (`DATABASE_URL`, `ALLOWED_ORIGIN`, `JWT_SECRET`, ...) con il file in cui sono definite. Non riportare valori.

## Regole

- Ogni blocco e ogni freccia deve poter essere ricondotto a un file.
- Le parti "da completare dopo il deploy" indicate nel README vanno segnate come tali, non riempite.
- Crea in nella cartella docs/ un file md con i diagrammi generati
