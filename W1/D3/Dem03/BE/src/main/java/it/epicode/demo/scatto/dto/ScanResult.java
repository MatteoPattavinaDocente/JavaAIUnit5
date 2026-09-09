package it.epicode.demo.scatto.dto;

public record ScanResult(String storageKey, long sizeBytes, String text, long millis) {
}
