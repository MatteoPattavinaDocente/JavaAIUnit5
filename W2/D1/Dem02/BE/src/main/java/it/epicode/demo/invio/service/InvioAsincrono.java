package it.epicode.demo.invio.service;

import it.epicode.demo.invio.dto.InvioRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Classe separata di proposito. @Async funziona tramite un proxy: se il metodo
 * annotato venisse chiamato da dentro EmailService, la chiamata non passerebbe
 * dal proxy e l'annotazione non farebbe nulla.
 */
@Service
public class InvioAsincrono {

	private final EmailService emailService;

	public InvioAsincrono(EmailService emailService) {
		this.emailService = emailService;
	}

	/**
	 * Ritorna void: chi chiama non aspetta e non sapra' mai come e' andata.
	 * L'esito finisce nel registro, che e' il motivo per cui il registro esiste
	 * (slide 22 e 23).
	 */
	@Async
	public void avvia(InvioRequest richiesta) {
		emailService.invia(richiesta);
	}
}
