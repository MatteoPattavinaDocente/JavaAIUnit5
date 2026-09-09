package it.epicode.demo.allegati.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "attachments")
public record AttachmentProperties(

        /** Cartella dove finiscono i file caricati. */
        String storageDir,

        /** Numero massimo di file per invio: non esiste un parametro Spring, va controllato nel codice. */
        int maxFiles) {
}
