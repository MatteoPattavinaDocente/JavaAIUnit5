package it.epicode.demo.ocr.web;

import it.epicode.demo.ocr.ExtractedText;
import it.epicode.demo.ocr.OcrException;
import it.epicode.demo.ocr.TextExtractor;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ScanController {

    private final TextExtractor textExtractor;

    ScanController(TextExtractor textExtractor) {
        this.textExtractor = textExtractor;
    }

    @PostMapping(path = "/api/extract", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ExtractedText extract(@RequestBody byte[] image,
                          @RequestParam(defaultValue = "it") String lang) {
        return textExtractor.extract(image, Locale.of(lang));
    }

    @ExceptionHandler(OcrException.class)
    ResponseEntity<Map<String, String>> handleOcrFailure(OcrException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "estrazione non riuscita", "detail", String.valueOf(e.getMessage())));
    }
}
