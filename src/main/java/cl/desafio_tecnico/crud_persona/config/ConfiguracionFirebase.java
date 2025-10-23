package cl.desafio_tecnico.crud_persona.config;

import com.google.cloud.firestore.Firestore;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Setter
@Configuration
public class ConfiguracionFirebase {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionFirebase.class);

    private Firestore firestoreInstance;
    private boolean inicializado = false;

    @Bean
    @Lazy
    public Firestore firestore() {
        logger.info("Configuración de Firestore diferida. La aplicación iniciará en modo offline hasta que se inicialice Firestore.");
        return null; // Diferir la inicialización a tiempo de ejecución
    }

    public boolean estaInicializado() {
        return inicializado && firestoreInstance != null;
    }

    public Firestore getFirestoreInstance() {
        return firestoreInstance;
    }

}