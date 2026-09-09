package it.epicode.demo.allegati.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Nel database salviamo solo i metadati: il contenuto del file sta sul filesystem.
 */
@Entity
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Documento a cui l'allegato appartiene. */
    private Long documentId;

    /** Nome scelto dall'utente: serve per il download, mai per salvare su disco. */
    private String originalName;

    /** Tipo reale, dedotto dai primi byte (non quello dichiarato dal client). */
    private String contentType;

    /** Nome generato con cui il file e' salvato su disco. */
    private String storageKey;

    private long sizeBytes;

    protected Attachment() {
        // richiesto da JPA
    }

    public Attachment(Long documentId, String originalName, String contentType, String storageKey, long sizeBytes) {
        this.documentId = documentId;
        this.originalName = originalName;
        this.contentType = contentType;
        this.storageKey = storageKey;
        this.sizeBytes = sizeBytes;
    }

    public Long getId() {
        return id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }
}
