package it.epicode.demo.scatto.ocr;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ocr")
public record OcrProperties(String tessdataPath, String language) {
}
