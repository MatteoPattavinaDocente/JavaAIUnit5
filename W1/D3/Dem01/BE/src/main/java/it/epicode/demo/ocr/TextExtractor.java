package it.epicode.demo.ocr;

import java.util.Locale;

public interface TextExtractor {

    ExtractedText extract(byte[] image, Locale language);
}
