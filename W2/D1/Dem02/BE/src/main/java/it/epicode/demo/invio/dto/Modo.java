package it.epicode.demo.invio.dto;

/** I quattro modi di comporre lo stesso messaggio. */
public enum Modo {
	/** SimpleMailMessage: solo testo, nessuna parte MIME. */
	TESTO,
	/** MimeMessageHelper: due parti alternative, testo e HTML. */
	HTML,
	/** Come HTML, piu' un file scaricabile. */
	ALLEGATO,
	/** Come HTML, piu' un'immagine mostrata dentro il corpo con cid:. */
	INLINE
}
