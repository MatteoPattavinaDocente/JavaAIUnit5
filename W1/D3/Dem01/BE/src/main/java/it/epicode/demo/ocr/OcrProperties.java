package it.epicode.demo.ocr;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ocr")
public record OcrProperties(

        /** Cartella che contiene i file .traineddata. */
        String tessdataPath,

        /** Lingua di default: "ita", oppure "ita+eng" per due lingue insieme. */
        String language) {
}
