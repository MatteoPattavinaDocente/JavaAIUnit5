# Mappa dei componenti — Demo 01, 02, 03

Mappa completa: componenti React → endpoint → classi Spring → Tesseract.
Vale per entrambe le versioni del frontend, `FE` (TSX) e `FEJSX` (JSX): l'albero dei
componenti è identico, cambiano solo le estensioni dei file.

**Legenda**

| Forma | Significato |
|---|---|
| rettangolo | componente React |
| esagono `{{ }}` | elemento del DOM che porta l'interazione |
| cilindro `[( )]` | endpoint HTTP |
| rettangolo con bordi tondi, in `BE` | classe Spring (`@RestController`, `@Component`, `@Service`) |
| romboide | libreria/processo esterno al nostro codice (tess4j, libtesseract, `tessdata`) |
| cilindro doppio | archiviazione (filesystem, PostgreSQL) |

**Dov'è Tesseract**: solo in Dem01 e Dem03. Dem02 non fa OCR — è la demo degli allegati
(più file, tipo reale dai magic bytes, metadati su PostgreSQL). Per questo nella sua mappa
Tesseract non compare: non è una dimenticanza, è la differenza fra le due demo.

La catena OCR, uguale in Dem01 e Dem03:

```
TesseractTextExtractor (nostro codice, unica classe che nomina Tesseract)
  └─ tess4j 5.16.0            classe net.sourceforge.tess4j.Tesseract, bean @Scope("prototype")
       └─ JNA                 ponte Java → libreria nativa
            └─ libtesseract   binario installato sul sistema (Tesseract-OCR)
                 └─ tessdata  file .traineddata: ita.traineddata, eng.traineddata
```

Due punti che spiegano il codice:
- **prototype, non singleton**: un'istanza di `Tesseract` non è thread-safe, quindi
  `OcrConfig` la dichiara `@Scope(SCOPE_PROTOTYPE)` e l'estrattore ne chiede una nuova a ogni
  richiesta via `ObjectProvider<Tesseract>`.
- **niente eccezioni della libreria fuori dall'adattatore**: `TesseractException`,
  `UnsatisfiedLinkError` e `NoClassDefFoundError` (Tesseract non installato o `tessdata-path`
  sbagliato) diventano tutte `OcrException`, che il controller traduce in **502 Bad Gateway**.

---

## Dem01 — OCR di un'immagine

`App` è l'unico componente: tutto lo stato (`file`, `anteprima`, `lingua`, `risultato`,
`errore`, `inCorso`) vive lì. L'immagine viaggia come corpo grezzo della richiesta
(`application/octet-stream`), senza multipart: è un file solo.

```mermaid
flowchart TD
  subgraph FE["FE / FEJSX — Vite :5173"]
    main["main.jsx<br/>StrictMode + createRoot"] --> App["App.jsx<br/>stato: file, anteprima, lingua,<br/>risultato, errore, inCorso"]
    input{{"input[type=file]<br/>onChange → scegli()"}}
    select{{"select lingua → it | en"}}
    btn{{"button Estrai testo<br/>onClick → estrai()"}}
    img{{"img anteprima<br/>createObjectURL"}}
    ta{{"textarea<br/>defaultValue = risultato.text"}}
    err{{"section.card.errore<br/>errore.error + detail"}}
    App --> input & select & btn & img & ta & err
  end

  btn -->|"POST octet-stream + ?lang=it"| api[("/api/extract")]

  subgraph BE["BE — Spring Boot :8080"]
    api --> ctrl("ScanController<br/>@PostMapping /api/extract<br/>@RequestBody byte[] + @RequestParam lang")
    ctrl -->|"extract(image, Locale.of(lang))"| iface("TextExtractor<br/>interfaccia: il controller<br/>non sa chi fa l'OCR")
    iface --> impl("TesseractTextExtractor<br/>@Component")
    cfg("OcrConfig<br/>@Bean @Scope prototype<br/>setDatapath + setLanguage") -.->|"ObjectProvider&lt;Tesseract&gt;"| impl
    props("OcrProperties<br/>ocr.tessdata-path<br/>ocr.language") --> cfg
    cors("CorsConfig<br/>origine :5173")
    ctrl -.->|"OcrException → 502"| errh("@ExceptionHandler<br/>{ error, detail }")
  end

  impl -->|"ImageIO.read → BufferedImage"| decode{{"decodifica<br/>PNG/JPEG/TIFF"}}
  impl -->|"setLanguage(ISO3: ita | eng)<br/>doOCR(BufferedImage)"| tess[/"tess4j 5.16.0<br/>net.sourceforge.tess4j.Tesseract"/]
  tess -->|"JNA"| nativa[/"libtesseract<br/>binario di sistema"/]
  nativa --> data[("tessdata/<br/>ita.traineddata, eng.traineddata")]
  tess -->|"String text"| impl
  impl -->|"ExtractedText(text, millis)"| ctrl
  api -->|"{ text, millis }"| ta
  errh -->|"502 { error, detail }"| err
```

---

## Dem02 — Allegati multipli (nessun OCR)

Qui non c'è Tesseract: la demo riguarda l'invio di **più file** e la loro conservazione.
Il tipo reale si legge dai primi byte, non da `Content-Type` dichiarato dal client; il
contenuto va sul filesystem, in PostgreSQL solo i metadati. Se un solo file è invalido
l'invio viene rifiutato **tutto**, prima di scrivere qualsiasi cosa.

```mermaid
flowchart TD
  subgraph FE["FE / FEJSX — Vite :5173"]
    main["main.jsx"] --> App["App.jsx<br/>stato: scelti, salvati,<br/>motivi, inCorso"]
    input{{"input[type=file] multiple"}}
    lista{{"ul → li per file scelto<br/>nome, KB, type dichiarato"}}
    btn{{"button Carica N file<br/>onClick → carica()"}}
    ok{{"card Accettati<br/>ul di AttachmentDto"}}
    ko{{"card.errore Rifiutati<br/>ul dei motivi"}}
    App --> input & lista & btn & ok & ko
  end

  btn -->|"POST multipart/form-data<br/>FormData files[]"| api[("/api/documents/{id}/attachments")]

  subgraph BE["BE — Spring Boot :8080"]
    api --> ctrl("AttachmentController<br/>@RequestPart(&quot;files&quot;) MultipartFile[]")
    ctrl -->|"store(id, files)"| svc("AttachmentService<br/>@Transactional<br/>1. valida tutto 2. poi scrive")
    svc --> check("FileTypeCheck<br/>magic bytes:<br/>89PNG · FFD8FF · %PDF")
    svc --> repo("AttachmentRepository<br/>JpaRepository")
    props("AttachmentProperties<br/>attachments.storage-dir<br/>attachments.max-files = 5") --> svc
    ctrl -.->|"RejectedFileException → 400"| e1("@ExceptionHandler<br/>{ error, reasons[] }")
    ctrl -.->|"MaxUploadSizeExceededException → 413"| e2("@ExceptionHandler<br/>limiti: 2MB file / 8MB richiesta")
    cors("CorsConfig<br/>origine :5173")
  end

  svc -->|"UUID + estensione<br/>Files.write"| disco[("./uploads<br/>contenuto dei file")]
  repo --> db[("PostgreSQL<br/>tabella attachment: solo metadati")]
  api -->|"200 AttachmentDto[]"| ok
  e1 --> ko
  e2 --> ko
```

---

## Dem03 — Dalla fotocamera al testo (scatto + salvataggio + OCR)

Le due demo precedenti si uniscono. Lato FE compare il primo componente figlio, `Camera`,
che isola `getUserMedia`, lo `stop()` dello stream nel cleanup e la riduzione del fotogramma
su canvas; comunica col padre con una sola prop, `onCapture(file)`.
Lato BE il controller **prima conserva** il file e **poi** chiama l'OCR: un errore di
Tesseract non deve far perdere lo scatto.

```mermaid
flowchart TD
  subgraph FE["FE / FEJSX — Vite :5173"]
    main["main.jsx"] --> App["App.jsx<br/>stato: scatto, risultato,<br/>testo, errore, inCorso"]
    App -->|"prop onCapture(file)"| Camera["Camera.jsx<br/>stato: attiva, errore<br/>ref: videoRef, streamRef"]
    video{{"video autoPlay playsInline muted<br/>srcObject = MediaStream"}}
    scatta{{"button Scatta<br/>canvas ≤ 2000px, JPEG 0.85"}}
    fallback{{"input capture=environment<br/>se NotAllowedError / NotFoundError"}}
    Camera --> video & scatta & fallback
    ta{{"textarea controllata<br/>value = testo"}}
    err{{"p.errore — HTTP status + corpo"}}
    App --> ta & err
    scatta -->|"File jpeg"| App
    fallback -->|"File da app foto"| App
  end

  App -->|"POST multipart FormData file"| api[("/api/documents/{id}/scan")]

  subgraph BE["BE — Spring Boot :8080"]
    api --> ctrl("ScanController<br/>@RequestPart(&quot;file&quot;) MultipartFile<br/>1. salva 2. estrai")
    ctrl -->|"extract(content, Locale.ITALIAN)"| iface("TextExtractor")
    iface --> impl("TesseractTextExtractor<br/>@Component")
    cfg("OcrConfig<br/>@Bean @Scope prototype") -.->|"ObjectProvider&lt;Tesseract&gt;"| impl
    props("OcrProperties<br/>ocr.tessdata-path<br/>ocr.language = ita") --> cfg
    ctrl -.->|"OcrException → 502"| errh("@ExceptionHandler<br/>{ error, detail }")
    cors("CorsConfig<br/>origine :5173")
  end

  ctrl -->|"UUID.jpg — Files.write<br/>prima dell'OCR"| disco[("./uploads")]
  impl -->|"doOCR(BufferedImage)"| tess[/"tess4j 5.16.0<br/>Tesseract (prototype)"/]
  tess -->|"JNA"| nativa[/"libtesseract"/]
  nativa --> data[("tessdata/ita.traineddata")]
  tess -->|"String text"| impl
  impl -->|"ExtractedText(text, millis)"| ctrl
  api -->|"ScanResult { storageKey, sizeBytes, text, millis }"| ta
  errh --> err
```

### Dem03 — la stessa cosa come sequenza

```mermaid
sequenceDiagram
  actor U as Utente
  participant C as Camera.jsx
  participant A as App.jsx
  participant SC as ScanController
  participant FS as ./uploads
  participant TX as TesseractTextExtractor
  participant T4 as tess4j → libtesseract

  U->>C: preme Scatta
  C->>C: canvas ≤ 2000px, toBlob JPEG 0.85
  C->>A: onCapture(file)
  A->>SC: POST /api/documents/1/scan (multipart)
  SC->>FS: Files.write(UUID.jpg) — prima l'archiviazione
  SC->>TX: extract(bytes, Locale.ITALIAN)
  TX->>TX: ObjectProvider.getObject() — istanza nuova, non thread-safe
  TX->>TX: ImageIO.read → BufferedImage
  TX->>T4: setLanguage("ita") + doOCR(image)
  alt riconoscimento riuscito
    T4-->>TX: testo
    TX-->>SC: ExtractedText(text, millis)
    SC-->>A: 200 ScanResult
    A->>U: textarea correggibile + tempo OCR
  else Tesseract assente o immagine illeggibile
    T4-->>TX: TesseractException / UnsatisfiedLinkError
    TX-->>SC: OcrException
    SC-->>A: 502 { error, detail }
    A->>U: p.errore — lo scatto resta comunque su disco
  end
```

---

## Riassunto

| | Dem01 | Dem02 | Dem03 |
|---|---|---|---|
| Componenti React | `App` | `App` | `App`, `Camera` |
| Prop scambiate | — | — | `onCapture(file)` |
| Invio | corpo grezzo `octet-stream` | multipart, `files[]` | multipart, `file` |
| Endpoint | `POST /api/extract` | `POST /api/documents/{id}/attachments` | `POST /api/documents/{id}/scan` |
| Classi BE | `ScanController`, `TextExtractor`, `TesseractTextExtractor`, `OcrConfig`, `OcrProperties` | `AttachmentController`, `AttachmentService`, `FileTypeCheck`, `AttachmentRepository` | tutte quelle di Dem01 + salvataggio nel controller |
| Tesseract | **sì** (tess4j 5.16.0) | **no** | **sì** (tess4j 5.16.0) |
| Archiviazione | nessuna | `./uploads` + PostgreSQL (metadati) | `./uploads` |
| Errori tradotti | 502 `OcrException` | 400 rifiuto, 413 troppo grande | 502 `OcrException` |

**Configurazione Tesseract** (`application.yml`, Dem01 e Dem03):

```yaml
ocr:
  tessdata-path: ${TESSDATA_PATH:C:/Program Files/Tesseract-OCR/tessdata}
  language: ${OCR_LANGUAGE:ita}
```

Percorsi `tessdata` per sistema: Windows `C:/Program Files/Tesseract-OCR/tessdata` ·
Linux `/usr/share/tesseract-ocr/5/tessdata` · macOS `/opt/homebrew/share/tessdata`.
Se il percorso è sbagliato o Tesseract non è installato, il sintomo è sempre lo stesso:
502 con `detail` = "Tesseract non disponibile: …".
