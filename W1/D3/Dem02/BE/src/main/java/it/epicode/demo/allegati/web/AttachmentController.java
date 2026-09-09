package it.epicode.demo.allegati.web;

import it.epicode.demo.allegati.dto.AttachmentDto;
import it.epicode.demo.allegati.service.AttachmentService;
import it.epicode.demo.allegati.service.RejectedFileException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@RestController
class AttachmentController {

    private final AttachmentService service;

    AttachmentController(AttachmentService service) {
        this.service = service;
    }

    /** Il nome della parte ("files") deve coincidere con quello usato dal client in FormData.append. */
    @PostMapping(path = "/api/documents/{id}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<AttachmentDto> upload(@PathVariable Long id, @RequestPart("files") MultipartFile[] files) {
        return service.store(id, files);
    }

    @GetMapping("/api/documents/{id}/attachments")
    List<AttachmentDto> list(@PathVariable Long id) {
        return service.list(id);
    }

    @ExceptionHandler(RejectedFileException.class)
    ResponseEntity<Map<String, Object>> handleRejected(RejectedFileException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "invio rifiutato", "reasons", e.getReasons()));
    }

    /** Oltre il limite configurato Spring solleva questa: senza traduzione l'utente vede uno stack trace. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, Object>> handleTooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "file troppo grande", "reasons",
                        List.of("limite configurato: 2MB per file, 8MB per richiesta")));
    }
}
