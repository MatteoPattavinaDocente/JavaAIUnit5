package it.epicode.demo.benvenuto.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

/**
 * Il collegamento dell'email di benvenuto atterra qui. In questa demo non c'e'
 * niente da verificare: nessun database, nessun token vero, nessuna scadenza —
 * quella parte e' la demo 4. Serve comunque una pagina di arrivo, altrimenti il
 * pulsante dell'email finisce sulla pagina di errore di Spring e la lezione si
 * chiude su un 404 che non spiega nulla.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@GetMapping(value = "/verify", produces = MediaType.TEXT_HTML_VALUE)
	public String verify(@RequestParam(defaultValue = "ESEMPIO") String token) {
		// Il token arriva dalla barra dell'indirizzo: va sempre trattato come
		// testo, mai riscritto dentro l'HTML cosi' com'e'.
		String sicuro = HtmlUtils.htmlEscape(token);
		return """
				<!doctype html>
				<html lang="it">
				<head>
				  <meta charset="utf-8">
				  <meta name="viewport" content="width=device-width, initial-scale=1">
				  <title>Collegamento ricevuto</title>
				</head>
				<body style="margin:0; padding:40px 20px; background:#f4f6f8;
				             font-family:system-ui, sans-serif; color:#1b2733;">
				  <div style="max-width:560px; margin:0 auto; background:#ffffff;
				              border-radius:8px; padding:32px;">
				    <h1 style="margin:0 0 12px; font-size:20px;">Collegamento ricevuto</h1>
				    <p style="margin:0 0 16px; font-size:15px; line-height:1.5;">
				      Il pulsante dell'email ha funzionato: la richiesta e' arrivata al
				      backend con il token <code style="background:#eef1f4; padding:2px 6px;
				      border-radius:4px;">%s</code>.
				    </p>
				    <p style="margin:0; font-size:15px; line-height:1.5; color:#5b6b7c;">
				      Qui la verifica si ferma: in questa demo il token e' un
				      valore di esempio. Salvarlo, controllarne la scadenza e attivare
				      l'utente e' il lavoro della demo 4.
				    </p>
				  </div>
				</body>
				</html>
				""".formatted(sicuro);
	}
}
