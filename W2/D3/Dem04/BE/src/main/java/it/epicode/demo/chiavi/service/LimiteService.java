package it.epicode.demo.chiavi.service;

import it.epicode.demo.chiavi.config.LlmProperties;
import it.epicode.demo.chiavi.model.ChiamataLlm;
import it.epicode.demo.chiavi.repository.ChiamataLlmRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Senza un limite per utente una sola persona puo' esaurire il budget del
 * progetto in poche ore (slide 42).
 *
 * Il controllo va fatto PRIMA della chiamata, con una stima; il consumo REALE
 * si registra DOPO, con i valori dichiarati da usage. Sono due momenti
 * distinti, ed e' il punto della slide.
 */
@Service
public class LimiteService {

	private static final Logger log = LoggerFactory.getLogger(LimiteService.class);

	/** Un token vale circa quattro caratteri: stima grossolana, ma serve solo a fermare gli abusi. */
	private static final int CARATTERI_PER_TOKEN = 4;

	private final ChiamataLlmRepository repository;
	private final int limiteGiornaliero;

	public LimiteService(ChiamataLlmRepository repository, LlmProperties proprieta) {
		this.repository = repository;
		this.limiteGiornaliero = proprieta.limiteGiornalieroToken();
	}

	public int stima(String testo, int maxTokens) {
		return (testo.length() / CARATTERI_PER_TOKEN) + maxTokens;
	}

	/**
	 * Prima della chiamata: se la stima sfonda il tetto, si ferma qui e il
	 * servizio esterno non viene nemmeno contattato.
	 */
	@Transactional(readOnly = true)
	public void verifica(String utente, int tokenStimati) {
		int usati = usatiOggi(utente);
		if (usati + tokenStimati > limiteGiornaliero) {
			log.warn("limite superato per {}: usati={} stimati={} limite={}",
					utente, usati, tokenStimati, limiteGiornaliero);
			throw new LimiteSuperatoException(limiteGiornaliero, usati);
		}
	}

	/** Dopo la chiamata: il consumo reale, con i valori di usage. */
	@Transactional
	public ChiamataLlm registra(String utente, String modello, int tokenIngresso, int tokenUscita,
			String esito, long durataMs) {
		var chiamata = repository.save(
				new ChiamataLlm(utente, modello, tokenIngresso, tokenUscita, esito, durataMs));
		log.info("registrata: utente={} modello={} in={} out={} esito={} durata={}ms",
				utente, modello, tokenIngresso, tokenUscita, esito, durataMs);
		return chiamata;
	}

	@Transactional(readOnly = true)
	public int usatiOggi(String utente) {
		return repository.tokenDelGiorno(utente, LocalDate.now(ZoneId.systemDefault()));
	}

	public int limiteGiornaliero() {
		return limiteGiornaliero;
	}
}
