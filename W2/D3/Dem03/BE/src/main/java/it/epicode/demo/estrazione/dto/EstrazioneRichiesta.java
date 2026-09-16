package it.epicode.demo.estrazione.dto;

public record EstrazioneRichiesta(String testo, Integer maxTokens, Boolean conSchema) {

	public EstrazioneRichiesta {
		if (testo == null || testo.isBlank()) {
			throw new IllegalArgumentException("testo: non puo' essere vuoto");
		}
		if (maxTokens != null && (maxTokens < 1 || maxTokens > 4096)) {
			throw new IllegalArgumentException("maxTokens: fra 1 e 4096");
		}
	}
}
