# Demo 4 · La mappa delle API per Swagger

## Prompt

Leggi tutti i controller in `be/src/main/java/it/epicode/salone/web`, i DTO in `dto/`, gli handler in `ApiExceptionHandler` e la catena dei filtri in `config/SicurezzaConfig.java`.

Genera la specifica **OpenAPI 3.1** in `openapi/salone.yaml` con:
- `info` (titolo, versione dal `pom.xml`), `servers` (locale sulla porta 8080 e il backend su Render indicato in `README.md`);
- un `tag` per controller;
- ogni endpoint con `operationId`, parametri di percorso e di query, corpo di richiesta con `$ref` a uno schema, e le risposte con i codici reali: successo, 400 per la validazione, 401, 403, 404, 429 dove si applicano;
- `components/schemas` ricavati dai DTO (tipi, `required`, `minLength`, `maxLength`, `format: email` dai vincoli di Bean Validation);
- `components/securitySchemes` con `bearerAuth` (HTTP bearer, JWT) e `security` applicato solo agli endpoint protetti; gli endpoint pubblici hanno `security: []`.

Poi crea `openapi/swagger.html`: una pagina che carica **Swagger UI** da un CDN (`swagger-ui-dist`) e legge `salone.yaml`.

Verifica: dopo la generazione descrivi come controllare la specifica (`npx @redocly/cli lint openapi/salone.yaml`, oppure incollarla su editor.swagger.io) e conta gli endpoint: devono essere **28**, come in `MAPPA.md`.

## Regole

- Il file è **generato dal codice**: ogni operazione porta in `description` il riferimento `Controller.metodo (file:riga)`.
- Non inventare campi di risposta che non trovi nei DTO o nei record restituiti.
- Alternativa da nominare alla fine: la libreria springdoc-openapi produce la stessa specifica a ogni avvio dell'applicazione, senza file da mantenere.
