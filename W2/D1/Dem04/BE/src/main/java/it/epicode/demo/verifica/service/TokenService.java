package it.epicode.demo.verifica.service;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;

/**
 * Il token va generato con un generatore crittografico, non con Random
 * (slide 39). Con Random i valori successivi sono prevedibili a partire dai
 * precedenti: chi ne vede uno puo' calcolare quello di un altro utente.
 */
@Service
public class TokenService {

	private static final SecureRandom RNG = new SecureRandom();

	// 32 byte: lo spazio e' troppo grande per essere provato a tentativi.
	private static final int BYTE = 32;

	public String genera() {
		byte[] grezzo = new byte[BYTE];
		RNG.nextBytes(grezzo);
		// L'encoder per URL evita i caratteri che l'indirizzo dovrebbe
		// codificare: niente +, / o = da sfuggire nella query string.
		return Base64.getUrlEncoder().withoutPadding().encodeToString(grezzo);
	}
}
