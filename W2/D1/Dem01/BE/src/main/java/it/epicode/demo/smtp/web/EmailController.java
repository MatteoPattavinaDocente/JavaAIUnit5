package it.epicode.demo.smtp.web;

import it.epicode.demo.smtp.dto.EsitoInvio;
import it.epicode.demo.smtp.dto.InvioRequest;
import it.epicode.demo.smtp.dto.StatoMail;
import it.epicode.demo.smtp.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {

	private final EmailService service;

	public EmailController(EmailService service) {
		this.service = service;
	}

	@GetMapping("/stato")
	public StatoMail stato() {
		return service.stato();
	}

	@PostMapping("/testo")
	public EsitoInvio testo(@RequestBody InvioRequest richiesta) {
		return service.inviaTesto(richiesta);
	}
}
