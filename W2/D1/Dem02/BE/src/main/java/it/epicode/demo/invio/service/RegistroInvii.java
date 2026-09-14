package it.epicode.demo.invio.service;

import it.epicode.demo.invio.dto.VoceRegistro;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * Il registro degli invii (slide 23). In produzione e' una tabella: qui basta
 * una lista in memoria, che si svuota al riavvio.
 *
 * CopyOnWriteArrayList perche' con @Async scrivono thread diversi da quello
 * della richiesta HTTP che legge.
 */
@Component
public class RegistroInvii {

	private final CopyOnWriteArrayList<VoceRegistro> voci = new CopyOnWriteArrayList<>();

	public void aggiungi(VoceRegistro voce) {
		voci.add(voce);
	}

	/** Le ultime venti, dalla piu' recente. */
	public List<VoceRegistro> ultime() {
		return voci.reversed().stream().limit(20).toList();
	}

	public void svuota() {
		voci.clear();
	}
}
