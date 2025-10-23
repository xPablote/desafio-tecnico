package cl.desafio_tecnico.crud_persona.service;

import cl.desafio_tecnico.crud_persona.config.ConfiguracionFirebase;
import cl.desafio_tecnico.crud_persona.exception.ExcepcionRutInmutable;
import cl.desafio_tecnico.crud_persona.model.OperacionPendiente;
import cl.desafio_tecnico.crud_persona.model.Persona;
import cl.desafio_tecnico.crud_persona.repository.RepositorioOperacionesPendientes;
import cl.desafio_tecnico.crud_persona.validator.ValidadorRut;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SincronizadorPendientes {
    private static final Logger logger = LoggerFactory.getLogger(SincronizadorPendientes.class);

    @Autowired
    private RepositorioOperacionesPendientes repositorioOperacionesPendientes;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfiguracionFirebase configuracionFirebase;

    @Autowired
    private ServicioPersonas servicioPersonas;

    private final ValidadorRut validadorRut = new ValidadorRut();

    @Scheduled(fixedRate = 30000)
    public void sincronizarOperaciones() throws Exception {
        logger.info("Iniciando sincronización de operaciones pendientes");

        Firestore db = servicioPersonas.inicializarFirestore();
        if (db == null || !configuracionFirebase.estaInicializado()) {
            logger.info("Firestore no disponible - Omitiendo sincronización, se reintentará en 30 segundos");
            return;
        }

        List<OperacionPendiente> operacionesPendientes = repositorioOperacionesPendientes.findAll();
        if (operacionesPendientes.isEmpty()) {
            logger.info("No hay operaciones pendientes para sincronizar");
            return;
        }

        logger.info("Procesando {} operaciones pendientes", operacionesPendientes.size());
        int exitosas = 0;
        int fallidas = 0;
        for (OperacionPendiente pendiente : operacionesPendientes) {
            logger.info("Procesando operación {} para RUT: {}", pendiente.getTipoOperacion(), pendiente.getRut());
            if (procesarOperacion(db, pendiente)) {
                repositorioOperacionesPendientes.delete(pendiente);
                exitosas++;
                logger.info("Operación de sincronizacion completada - RUT: {}, Tipo: {}", pendiente.getRut(), pendiente.getTipoOperacion());
            } else {
                fallidas++;
                logger.warn("Operación falló - RUT: {}, Tipo: {}", pendiente.getRut(), pendiente.getTipoOperacion());
            }
        }
        logger.info("Sincronización completada - Exitosas: {}, Fallidas: {}", exitosas, fallidas);
    }

    private boolean procesarOperacion(Firestore db, OperacionPendiente pendiente) throws Exception {
        if (!pendiente.getTipoOperacion().equals("ELIMINAR")) {
            if (pendiente.getDatos() == null || pendiente.getDatos().trim().isEmpty() || pendiente.getDatos().equals("{}")) {
                logger.error("Datos vacíos o inválidos para operación {} - RUT: {}. Eliminando operación pendiente.", pendiente.getTipoOperacion(), pendiente.getRut());
                repositorioOperacionesPendientes.delete(pendiente);
                return false;
            }
        }

        return switch (pendiente.getTipoOperacion()) {
            case "CREAR" -> procesarCrear(db, pendiente);
            case "ACTUALIZAR" -> procesarActualizar(db, pendiente);
            case "ELIMINAR" -> procesarEliminar(db, pendiente);
            default -> {
                logger.warn("Tipo de operación desconocido: {}. Eliminando operación pendiente.", pendiente.getTipoOperacion());
                repositorioOperacionesPendientes.delete(pendiente);
                yield false;
            }
        };
    }

    private boolean procesarCrear(Firestore db, OperacionPendiente pendiente) throws Exception {
        logger.debug("Datos a deserializar para CREAR: {}", pendiente.getDatos());
        Persona persona = objectMapper.readValue(pendiente.getDatos(), Persona.class);
        if (persona.getRut() == null || persona.getRut().trim().isEmpty()) {
            logger.error("Persona sin RUT válido en operación CREAR - RUT: {}. Eliminando operación pendiente.", pendiente.getRut());
            repositorioOperacionesPendientes.delete(pendiente);
            return false;
        }
        boolean existe = db.collection("personas").document(persona.getRut()).get().get().exists();
        if (existe) {
            logger.info("RUT ya existe, sin sincronización - RUT: {}", persona.getRut());
            return true;
        }
        db.collection("personas").document(persona.getRut()).set(persona).get();
        logger.info("Persona creada exitosamente en sincronización - RUT: {}", persona.getRut());
        return true;
    }

    private boolean procesarActualizar(Firestore db, OperacionPendiente pendiente) throws Exception {
        logger.debug("Datos a deserializar para ACTUALIZAR: {}", pendiente.getDatos());
        Persona persona = objectMapper.readValue(pendiente.getDatos(), Persona.class);
        if (persona.getRut() == null || persona.getRut().trim().isEmpty()) {
            logger.error("Persona sin RUT válido en operación ACTUALIZAR - RUT: {}. Eliminando operación pendiente.", pendiente.getRut());
            repositorioOperacionesPendientes.delete(pendiente);
            return false;
        }
        if (!pendiente.getRut().equals(persona.getRut())) {
            logger.info("Intento de actualizar RUT de {} a {} en sincronización. Acción bloqueada.", pendiente.getRut(), persona.getRut());
            throw new ExcepcionRutInmutable("No se permite actualizar el RUT de una persona en sincronización");
        }
        boolean existe = db.collection("personas").document(pendiente.getRut()).get().get().exists();
        if (!existe) {
            logger.info("Persona no encontrada para actualizar en sincronización - RUT: {}. Eliminando operación pendiente.", pendiente.getRut());
            repositorioOperacionesPendientes.delete(pendiente);
            return false;
        }
        db.collection("personas").document(pendiente.getRut()).set(persona).get();
        logger.info("Persona actualizada exitosamente en sincronización - RUT: {}", pendiente.getRut());
        return true;
    }

    private boolean procesarEliminar(Firestore db, OperacionPendiente pendiente) throws Exception {
        String rut = pendiente.getRut();
        if (!validadorRut.isValid(rut, null)) {
            logger.info("RUT inválido en sincronización: {}. Eliminando operación pendiente.", rut);
            repositorioOperacionesPendientes.delete(pendiente);
            return false;
        }
        boolean existe = db.collection("personas").document(rut).get().get().exists();
        if (!existe) {
            logger.info("Persona no existe para eliminar en sincronización - RUT: {}. Eliminando operación pendiente.", rut);
            repositorioOperacionesPendientes.delete(pendiente);
            return true;
        }
        db.collection("personas").document(rut).delete().get();
        logger.info("Persona eliminada exitosamente en sincronización - RUT: {}", rut);
        return true;
    }
}