package it.epicode.demo.scatto.ocr;

import java.util.Locale;

public interface TextExtractor {

    ExtractedText extract(byte[] image, Locale language);
}
