package it.epicode.demo.invio.web;

import it.epicode.demo.invio.dto.EsitoInvio;
import it.epicode.demo.invio.dto.InvioRequest;
import it.epicode.demo.invio.dto.VoceRegistro;
import it.epicode.demo.invio.service.EmailService;
import it.epicode.demo.invio.service.InvioAsincrono;
import it.epicode.demo.invio.service.RegistroInvii;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {

	private final EmailService emailService;
	private final InvioAsincrono invioAsincrono;
	private final RegistroInvii registro;

	public EmailController(EmailService emailService, InvioAsincrono invioAsincrono, RegistroInvii registro) {
		this.emailService = emailService;
		this.invioAsincrono = invioAsincrono;
		this.registro = registro;
	}

	@PostMapping("/invia")
	public EsitoInvio invia(@RequestBody InvioRequest richiesta) {
		long inizio = System.nanoTime();

		if (richiesta.asincrono()) {
			// Torna subito: l'invio prosegue su un altro thread.
			invioAsincrono.avvia(richiesta);
			long millisRichiesta = (System.nanoTime() - inizio) / 1_000_000;
			return new EsitoInvio(richiesta.modo().name(), true, true, millisRichiesta, -1,
					"invio preso in carico, l'esito finisce nel registro", null, null);
		}

		// L'invio avviene dentro la richiesta HTTP: la pagina aspetta il server
		// di posta (slide 22).
		EsitoInvio esito = emailService.invia(richiesta);
		long millisRichiesta = (System.nanoTime() - inizio) / 1_000_000;
		return new EsitoInvio(esito.modo(), false, esito.accettato(), millisRichiesta,
				esito.millisInvio(), esito.messaggio(), esito.eccezione(), esito.causa());
	}

	@GetMapping("/registro")
	public List<VoceRegistro> registro() {
		return registro.ultime();
	}

	@DeleteMapping("/registro")
	public void svuota() {
		registro.svuota();
	}
}
