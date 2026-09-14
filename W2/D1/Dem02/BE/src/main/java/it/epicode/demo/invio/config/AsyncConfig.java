package it.epicode.demo.invio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Senza questa annotazione @Async non fa nulla: il metodo viene eseguito sullo
 * stesso thread della richiesta e l'annotazione resta un commento colorato.
 *
 * Con spring.threads.virtual.enabled a true (application.yml) i compiti @Async
 * girano su thread virtuali: l'attesa di rete costa poco anche in serie
 * (slide 22).
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
