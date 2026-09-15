package it.epicode.demo.client.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Un registro di token in memoria, al posto di Spring Security che nel corso
 * arriva piu' avanti. POST /api/login consegna un token, l'interceptor lo
 * riconosce sul frame CONNECT.
 *
 * Non c'e' nessun controllo di password: il punto della giornata e' DOVE viaggia
 * il token, non come si verifica una credenziale.
 */
@Service
public class TokenStore {

	private static final SecureRandom RNG = new SecureRandom();

	private final Map<String, String> utentePerToken = new ConcurrentHashMap<>();

	public String emetti(String utente) {
		byte[] grezzo = new byte[24];
		RNG.nextBytes(grezzo);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(grezzo);
		utentePerToken.put(token, utente);
		return token;
	}

	public Optional<String> utenteDi(String token) {
		return Optional.ofNullable(utentePerToken.get(token));
	}
}
