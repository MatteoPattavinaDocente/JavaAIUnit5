package it.epicode.demo.allegati.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.epicode.demo.allegati.config.AttachmentProperties;
import it.epicode.demo.allegati.dto.AttachmentDto;
import it.epicode.demo.allegati.model.Attachment;
import it.epicode.demo.allegati.repository.AttachmentRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@EnableConfigurationProperties(AttachmentProperties.class)
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository repository;
    private final AttachmentProperties properties;
    private final Path storageDir;

    AttachmentService(AttachmentRepository repository, AttachmentProperties properties) {
        this.repository = repository;
        this.properties = properties;
        this.storageDir = Path.of(properties.storageDir()).toAbsolutePath();
        createStorageDir();
    }

    @Transactional
    public List<AttachmentDto> store(Long documentId, MultipartFile[] files) {
        validate(files);

        List<AttachmentDto> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] content = read(file);
            String realType = FileTypeCheck.detect(content);
            String storageKey = UUID.randomUUID() + extensionOf(realType);

            write(storageKey, content);

            Attachment attachment = repository.save(new Attachment(
                    documentId, safeName(file.getOriginalFilename()), realType, storageKey, content.length));
            log.info("Salvato {} come {} ({} byte, tipo reale {})",
                    attachment.getOriginalName(), storageKey, content.length, realType);
            saved.add(AttachmentDto.from(attachment));
        }
        return saved;
    }

    public List<AttachmentDto> list(Long documentId) {
        return repository.findByDocumentId(documentId).stream().map(AttachmentDto::from).toList();
    }

    /** Prima si controlla tutto, poi si scrive: cosi' non restano file orfani se un file e' invalido. */
    private void validate(MultipartFile[] files) {
        List<String> reasons = new ArrayList<>();

        if (files.length == 0) {
            reasons.add("nessun file ricevuto");
        }
        if (files.length > properties.maxFiles()) {
            reasons.add("troppi file: massimo " + properties.maxFiles());
        }
        for (MultipartFile file : files) {
            String name = safeName(file.getOriginalFilename());
            if (file.isEmpty()) {
                reasons.add(name + ": file vuoto");
                continue;
            }
            String realType = FileTypeCheck.detect(read(file));
            if (realType == null) {
                reasons.add(name + ": formato non ammesso (il client dichiara " + file.getContentType() + ")");
            } else if (!realType.equals(file.getContentType())) {
                reasons.add(name + ": il contenuto e' " + realType
                        + " ma il client dichiara " + file.getContentType());
            }
        }
        if (!reasons.isEmpty()) {
            throw new RejectedFileException(reasons);
        }
    }

    /** Il nome scelto dall'utente puo' contenere percorsi: teniamo solo l'ultima parte. */
    private String safeName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "senza-nome";
        }
        return Path.of(originalFilename.replace('\\', '/')).getFileName().toString();
    }

    private String extensionOf(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
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
            log.info("Cartella allegati: {}", storageDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
