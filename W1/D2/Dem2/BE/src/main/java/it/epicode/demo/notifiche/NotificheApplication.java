package it.epicode.demo.notifiche;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Il punto di partenza dell'applicazione.
 *
 * @SpringBootApplication accende Spring e gli dice di guardare dentro questo package
 * e in tutti i sottopackage: ogni classe annotata (@RestController, @Service,
 * @Repository, @Configuration) viene trovata e creata automaticamente all'avvio.
 */
@SpringBootApplication
public class NotificheApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificheApplication.class, args);
	}
}
