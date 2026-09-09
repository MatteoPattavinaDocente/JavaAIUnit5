package it.epicode.demo.ocr;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@EnableConfigurationProperties(OcrProperties.class)
class OcrConfig {

    /**
     * Un'istanza di Tesseract NON e' utilizzabile da piu' thread contemporaneamente:
     * con lo scope prototype ne otteniamo una nuova a ogni richiesta.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Tesseract tesseract(OcrProperties properties) {
        var tesseract = new Tesseract();
        tesseract.setDatapath(properties.tessdataPath());
        tesseract.setLanguage(properties.language());
        return tesseract;
    }
}
