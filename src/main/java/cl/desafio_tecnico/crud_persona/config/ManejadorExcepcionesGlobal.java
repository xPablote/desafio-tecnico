package cl.desafio_tecnico.crud_persona.config;

import cl.desafio_tecnico.crud_persona.exception.ExcepcionFirestoreNoDisponible;
import cl.desafio_tecnico.crud_persona.exception.ExcepcionRutDuplicado;
import cl.desafio_tecnico.crud_persona.exception.ExcepcionRutInmutable;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@ControllerAdvice
public class ManejadorExcepcionesGlobal {
    private static final Logger logger = LoggerFactory.getLogger(ManejadorExcepcionesGlobal.class);

    @ExceptionHandler(ExcepcionFirestoreNoDisponible.class)
    public ResponseEntity<String> manejarFirestoreNoDisponible(ExcepcionFirestoreNoDisponible e) {
        logger.info("Firestore no disponible: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(e.getMessage());
    }

    @ExceptionHandler(ExcepcionRutDuplicado.class)
    public ResponseEntity<String> manejarRutDuplicado(ExcepcionRutDuplicado e) {
        logger.info("RUT duplicado o inválido: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> manejarValidacion(ConstraintViolationException e) {
        logger.info("Datos de entrada inválidos: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Datos de entrada inválidos: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarNoEncontrado(IllegalArgumentException e) {
        logger.info("Recurso no encontrado: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<String> manejarJsonProcessing(JsonProcessingException e) {
        logger.error("Error de formato en los datos: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error de formato en los datos: " + e.getMessage());
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<String> manejarInvalidFormatException(InvalidFormatException e) {
        logger.error("Formato inválido en los datos (por ejemplo, fecha incorrecta): {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato inválido en los datos (por ejemplo, fecha incorrecta): " + e.getMessage());
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<String> manejarExecutionException(ExecutionException e) {
        logger.error("Error al ejecutar la operación en Firestore: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al ejecutar la operación en Firestore");
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> manejarInterruptedException(InterruptedException e) {
        logger.error("La sincronización fue interrumpida: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("La sincronización fue interrumpida");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> manejarIOException(IOException e) {
        logger.error("Error de entrada/salida al inicializar Firestore: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Firestore no disponible, operación guardada temporalmente");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErrorGeneral(Exception e) {
        logger.error("Error interno del servidor: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud");
    }

    @ExceptionHandler(java.text.ParseException.class)
    public ResponseEntity<String> manejarParseException(java.text.ParseException e) {
        logger.error("Formato de fecha inválido, esperado dd-MM-yyyy: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de fecha inválido, esperado dd-MM-yyyy: " + e.getMessage());
    }

    @ExceptionHandler(ExcepcionRutInmutable.class)
    public ResponseEntity<String> manejarRutInmutable(ExcepcionRutInmutable e) {
        logger.info("No se permite actualizar el RUT: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}