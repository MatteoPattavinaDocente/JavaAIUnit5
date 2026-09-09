package it.epicode.demo.scatto.ocr;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@EnableConfigurationProperties(OcrProperties.class)
class OcrConfig {

    /** Prototype: un'istanza di Tesseract non e' condivisibile fra thread. */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Tesseract tesseract(OcrProperties properties) {
        var tesseract = new Tesseract();
        tesseract.setDatapath(properties.tessdataPath());
        tesseract.setLanguage(properties.language());
        return tesseract;
    }
}
