package it.epicode.demo.scatto.web;

import it.epicode.demo.scatto.dto.ScanResult;
import it.epicode.demo.scatto.ocr.ExtractedText;
import it.epicode.demo.scatto.ocr.OcrException;
import it.epicode.demo.scatto.ocr.TextExtractor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Le tre parti della lezione si uniscono: lo scatto arriva come allegato,
 * viene conservato e solo dopo passa all'estrattore di testo.
 */
@RestController
class ScanController {

    private static final Logger log = LoggerFactory.getLogger(ScanController.class);

    private final TextExtractor textExtractor;
    private final Path storageDir;

    ScanController(TextExtractor textExtractor, @Value("${scan.storage-dir}") String storageDir) {
        this.textExtractor = textExtractor;
        this.storageDir = Path.of(storageDir).toAbsolutePath();
        createStorageDir();
    }

    @PostMapping(path = "/api/documents/{id}/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ScanResult scan(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        byte[] content = read(file);

        // Prima si conserva il file: un errore dell'OCR non deve far perdere lo scatto.
        String storageKey = UUID.randomUUID() + ".jpg";
        write(storageKey, content);
        log.info("Documento {}: ricevuto scatto di {} byte, salvato come {}", id, content.length, storageKey);

        ExtractedText extracted = textExtractor.extract(content, Locale.ITALIAN);
        return new ScanResult(storageKey, content.length, extracted.text(), extracted.millis());
    }

    @ExceptionHandler(OcrException.class)
    ResponseEntity<Map<String, String>> handleOcrFailure(OcrException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "estrazione non riuscita", "detail", String.valueOf(e.getMessage())));
    }

    private byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void write(String storageKey, byte[] content) {
        try {
            Files.write(storageDir.resolve(storageKey), content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createStorageDir() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
