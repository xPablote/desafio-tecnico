package cl.desafio_tecnico.crud_persona;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories("cl.desafio_tecnico.crud_persona.repository")
@EntityScan("cl.desafio_tecnico.crud_persona.model")
public class CrudPersonaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrudPersonaApplication.class, args);
	}

}
