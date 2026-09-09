package it.epicode.demo.scatto.ocr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
class TesseractTextExtractor implements TextExtractor {

    private static final Logger log = LoggerFactory.getLogger(TesseractTextExtractor.class);

    private final ObjectProvider<Tesseract> tesseractProvider;

    TesseractTextExtractor(ObjectProvider<Tesseract> tesseractProvider) {
        this.tesseractProvider = tesseractProvider;
    }

    @Override
    public ExtractedText extract(byte[] image, Locale language) {
        long start = System.currentTimeMillis();
        Tesseract tesseract = tesseractProvider.getObject();
        tesseract.setLanguage(language.getISO3Language());
        try {
            String text = tesseract.doOCR(decode(image));
            long millis = System.currentTimeMillis() - start;
            log.info("OCR completato in {} ms, {} caratteri", millis, text.length());
            return new ExtractedText(text.strip(), millis);
        } catch (TesseractException e) {
            throw new OcrException("Tesseract non e' riuscito a leggere l'immagine", e);
        } catch (IllegalArgumentException | UnsatisfiedLinkError | NoClassDefFoundError e) {
            // Tesseract non installato, oppure percorso tessdata sbagliato: fuori esce
            // sempre un errore di dominio, mai un problema della libreria nativa.
            throw new OcrException("Tesseract non disponibile: " + e.getMessage(), e);
        }
    }

    private BufferedImage decode(byte[] image) {
        try {
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(image));
            if (decoded == null) {
                throw new OcrException("Formato immagine non riconosciuto", null);
            }
            return decoded;
        } catch (IOException e) {
            throw new OcrException("Immagine illeggibile", e);
        }
    }
}
