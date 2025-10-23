package cl.desafio_tecnico.crud_persona.service;

import cl.desafio_tecnico.crud_persona.config.ConfiguracionFirebase;
import cl.desafio_tecnico.crud_persona.dto.PersonaResponseDTO;
import cl.desafio_tecnico.crud_persona.exception.ExcepcionFirestoreNoDisponible;
import cl.desafio_tecnico.crud_persona.exception.ExcepcionRutDuplicado;
import cl.desafio_tecnico.crud_persona.exception.ExcepcionRutInmutable;
import cl.desafio_tecnico.crud_persona.model.OperacionPendiente;
import cl.desafio_tecnico.crud_persona.model.Persona;
import cl.desafio_tecnico.crud_persona.repository.RepositorioOperacionesPendientes;
import cl.desafio_tecnico.crud_persona.validator.ValidadorRut;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ServicioPersonas {
    private static final Logger logger = LoggerFactory.getLogger(ServicioPersonas.class);

    @Autowired(required = false)
    private Firestore firestore;

    @Autowired
    private RepositorioOperacionesPendientes repositorioOperacionesPendientes;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfiguracionFirebase configuracionFirebase;

    private final ValidadorRut validadorRut = new ValidadorRut();

    public Firestore inicializarFirestore() throws IOException {
        if (firestore != null && configuracionFirebase.estaInicializado()) {
            logger.info("Firestore ya inicializado, reutilizando instancia.");
            return firestore;
        }

        InputStream serviceAccount = this.getClass().getClassLoader().getResourceAsStream("serviceAccountKey1.json");
        if (serviceAccount == null) {
            logger.info("Archivo de credenciales no encontrado en recursos. Operando en modo offline.");
            configuracionFirebase.setInicializado(false);
            return null;
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp firebaseApp;
        if (FirebaseApp.getApps().isEmpty()) {
            firebaseApp = FirebaseApp.initializeApp(options);
        } else {
            firebaseApp = FirebaseApp.getApps().get(0);
        }

        firestore = FirestoreClient.getFirestore(firebaseApp);
        configuracionFirebase.setInicializado(true);
        configuracionFirebase.setFirestoreInstance(firestore);
        logger.info("Firestore inicializado correctamente en tiempo de ejecución");
        return firestore;
    }

    private boolean isFirestoreDisponible() throws ExcepcionFirestoreNoDisponible, ExecutionException, InterruptedException {
        if (firestore == null || !configuracionFirebase.estaInicializado()) {
            throw new ExcepcionFirestoreNoDisponible("Firestore no inicializado");
        }
        // Prueba mínima para verificar si la base de datos existe
        firestore.collection("test").document("test").get().get();
        return true;
    }

    public PersonaResponseDTO crearPersona(Persona persona) throws Exception {
        logger.info("CREAR PERSONA - Iniciando para RUT: {}", persona.getRut());
        firestore = inicializarFirestore();
        if (firestore == null) {
            logger.info("Firestore no disponible - Guardando operación pendiente para RUT: {}", persona.getRut());
            String personaJson = objectMapper.writeValueAsString(persona);
            OperacionPendiente pendiente = new OperacionPendiente(null, persona.getRut(), "CREAR", personaJson);
            repositorioOperacionesPendientes.save(pendiente);
            logger.info("Operación crear pendiente GUARDADA en H2 para RUT: {}", persona.getRut());
            throw new ExcepcionFirestoreNoDisponible("Operación guardada temporalmente");
        }
        if (!isFirestoreDisponible()) {
            logger.info("Firestore no disponible - Guardando operación pendiente para RUT: {}", persona.getRut());
            String personaJson = objectMapper.writeValueAsString(persona);
            OperacionPendiente pendiente = new OperacionPendiente(null, persona.getRut(), "CREAR", personaJson);
            repositorioOperacionesPendientes.save(pendiente);
            logger.info("Operación crear pendiente GUARDADA en H2 para RUT: {}", persona.getRut());
            throw new ExcepcionFirestoreNoDisponible("Operación guardada temporalmente");
        }
        boolean existe = firestore.collection("personas").document(persona.getRut()).get().get().exists();
        if (existe) {
            logger.info("RUT duplicado - Ya existe: {}", persona.getRut());
            throw new ExcepcionRutDuplicado("El RUT ya está registrado");
        }
        firestore.collection("personas").document(persona.getRut()).set(persona).get();
        logger.info("Persona creada exitosamente - RUT: {}", persona.getRut());
        return new PersonaResponseDTO(persona);
    }

    public PersonaResponseDTO actualizarPersona(String rut, Persona persona) throws Exception {
        logger.info("ACTUALIZAR PERSONA - Iniciando para RUT: {}", rut);
        if (!rut.equals(persona.getRut())) {
            logger.info("Intento de actualizar RUT de {} a {}. Acción bloqueada.", rut, persona.getRut());
            throw new ExcepcionRutInmutable("No se permite actualizar el RUT de una persona");
        }
        firestore = inicializarFirestore();
        if (firestore == null) {
            logger.info("Firestore no disponible - Guardando operación pendiente para RUT: {}", rut);
            String personaJson = objectMapper.writeValueAsString(persona);
            OperacionPendiente pendiente = new OperacionPendiente(null, rut, "ACTUALIZAR", personaJson);
            repositorioOperacionesPendientes.save(pendiente);
            logger.info("Operación actualizar pendiente GUARDADA en H2 para RUT: {}", rut);
            throw new ExcepcionFirestoreNoDisponible("Operación guardada temporalmente");
        }
        if (!isFirestoreDisponible()) {
            logger.info("Firestore no disponible - Guardando operación pendiente para RUT: {}", rut);
            String personaJson = objectMapper.writeValueAsString(persona);
            OperacionPendiente pendiente = new OperacionPendiente(null, rut, "ACTUALIZAR", personaJson);
            repositorioOperacionesPendientes.save(pendiente);
            logger.info("Operación actualizar pendiente GUARDADA en H2 para RUT: {}", rut);
            throw new ExcepcionFirestoreNoDisponible("Operación guardada temporalmente");
        }
        boolean existe = firestore.collection("personas").document(rut).get().get().exists();
        if (!existe) {
            logger.info("Persona no encontrada para actualizar - RUT: {}", rut);
            throw new IllegalArgumentException("Persona no encontrada");
        }

        firestore.collection("personas").document(rut).set(persona).get();
        logger.info("Persona actualizada exitosamente - RUT: {}", rut);

        return new PersonaResponseDTO(persona);
    }

    public void eliminarPersona(String rut) throws Exception {
        logger.info("ELIMINAR PERSONA - Iniciando para RUT: {}", rut);
        if (!validadorRut.isValid(rut, null)) {
            logger.info("RUT inválido: {}", rut);
            throw new ExcepcionRutDuplicado("RUT inválido");
        }
        firestore = inicializarFirestore();
        if (firestore == null) {
            logger.info("Firestore no disponible - Guardando operación pendiente para RUT: {}", rut);
            OperacionPendiente pendiente = new OperacionPendiente(null, rut, "ELIMINAR", "{}");
            repositorioOperacionesPendientes.save(pendiente);
            logger.info("Operación eliminar pendiente GUARDADA en H2 para RUT: {}", rut);
            throw new ExcepcionFirestoreNoDisponible("Operación guardada temporalmente");
        }
        if (!isFirestoreDisponible()) {
            logger.info("Firestore no disponible - Guardando operación pendiente para RUT: {}", rut);
            OperacionPendiente pendiente = new OperacionPendiente(null, rut, "ELIMINAR", "{}");
            repositorioOperacionesPendientes.save(pendiente);
            logger.info("Operación eliminar pendiente GUARDADA en H2 para RUT: {}", rut);
            throw new ExcepcionFirestoreNoDisponible("Operación guardada temporalmente");
        }
        boolean existe = firestore.collection("personas").document(rut).get().get().exists();
        if (!existe) {
            logger.info("Persona no encontrada para eliminar - RUT: {}", rut);
            throw new IllegalArgumentException("Persona no encontrada");
        }
        firestore.collection("personas").document(rut).delete().get();
        logger.info("Persona eliminada exitosamente - RUT: {}", rut);
    }

    public PersonaResponseDTO obtenerPersonaPorRut(String rut) throws Exception {
        logger.info("OBTENER PERSONA - Buscando RUT: {}", rut);
        if (!validadorRut.isValid(rut, null)) {
            logger.info("RUT inválido: {}", rut);
            throw new ExcepcionRutDuplicado("RUT inválido");
        }
        firestore = inicializarFirestore();
        if (firestore == null || !isFirestoreDisponible()) {
            logger.info("Firestore no disponible - No se puede obtener la persona con RUT: {}", rut);
            throw new ExcepcionFirestoreNoDisponible("Firestore no disponible");
        }
        var documentSnapshot = firestore.collection("personas").document(rut).get().get();
        if (!documentSnapshot.exists()) {
            logger.info("Persona no encontrada - RUT: {}", rut);
            return null;
        }
        Persona persona = documentSnapshot.toObject(Persona.class);
        logger.info("Persona encontrada - RUT: {}", rut);
        return new PersonaResponseDTO(persona);
    }

    public List<PersonaResponseDTO> obtenerPersonas() throws Exception {
        logger.info("OBTENER TODAS LAS PERSONAS - Iniciando consulta");
        firestore = inicializarFirestore();
        if (firestore == null || !isFirestoreDisponible()) {
            logger.info("Firestore no disponible - No se puede obtener la lista de personas");
            throw new ExcepcionFirestoreNoDisponible("Firestore no disponible");
        }
        List<PersonaResponseDTO> personas = new ArrayList<>();
        var querySnapshot = firestore.collection("personas").get().get();
        for (var document : querySnapshot.getDocuments()) {
            Persona persona = document.toObject(Persona.class);
            personas.add(new PersonaResponseDTO(persona));
        }
        logger.info("Consulta completada - {} personas obtenidas", personas.size());
        return personas;
    }
}