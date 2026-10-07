# Demo 4 · La mappa delle API per Scalar

Da usare dopo il prompt Swagger: Scalar legge la **stessa specifica**, senza un secondo file da mantenere.

## Prompt

Parti da `openapi/salone.yaml`, già generato. Non modificarlo e non duplicarlo.

Crea `openapi/scalar.html`: una pagina che carica **Scalar API Reference** da un CDN (`@scalar/api-reference`) e legge `salone.yaml`. Configurala così:
- tema scuro e layout moderno;
- autenticazione preimpostata sullo schema `bearerAuth`, con il campo del token vuoto;
- server predefinito: quello locale sulla porta 8080;
- client di esempio predefinito: `curl`, con `fetch` di JavaScript come secondo;
- gli endpoint raggruppati per `tag`, con `Pubblici`, `Utente`, `Admin` in quell'ordine se la specifica li distingue.

Poi spiega in cinque righe:
1. come servire i due file in locale senza installare nulla (per esempio `npx serve openapi`) e perché aprirli con doppio clic non basta (caricamento del file YAML e CORS);
2. come provare una chiamata protetta: fare il login, copiare il token, incollarlo nel campo di autenticazione;
3. la differenza pratica fra Swagger UI e Scalar per chi legge la documentazione.

## Regole

- Una sola fonte: se l'API cambia si rigenera `salone.yaml` e Scalar si aggiorna da solo.
- Nessun token o password nella pagina.
- Dichiara la versione della libreria che hai usato dal CDN: non scrivere `latest` senza dirlo.
