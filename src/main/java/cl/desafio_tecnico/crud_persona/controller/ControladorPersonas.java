package cl.desafio_tecnico.crud_persona.controller;

import cl.desafio_tecnico.crud_persona.dto.PersonaResponseDTO;
import cl.desafio_tecnico.crud_persona.model.Persona;
import cl.desafio_tecnico.crud_persona.service.ServicioPersonas;
import cl.desafio_tecnico.crud_persona.validator.ValidadorRut;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/personas")
public class ControladorPersonas {
    private static final Logger logger = LoggerFactory.getLogger(ControladorPersonas.class);

    @Autowired
    private ServicioPersonas servicioPersonas;

    @Autowired
    private Validator validator;

    private final ValidadorRut validadorRut = new ValidadorRut();

    @Operation(summary = "Crear una nueva persona", description = "Crea una persona en la base de datos con RUT único como identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Persona creada con éxito",
                    content = @Content(schema = @Schema(implementation = PersonaResponseDTO.class))),
            @ApiResponse(responseCode = "202", description = "Operación guardada temporalmente debido a indisponibilidad de Firestore",
                    content = @Content(examples = @ExampleObject(value = "\"Operación guardada temporalmente\""))),
            @ApiResponse(responseCode = "409", description = "El RUT ya está registrado o es inválido",
                    content = @Content(examples = @ExampleObject(value = "\"El RUT ya está registrado\""))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(examples = @ExampleObject(value = "\"Error al procesar la solicitud\"")))
    })
    @PostMapping
    public ResponseEntity<PersonaResponseDTO> crearPersona(@RequestBody Persona persona) throws Exception {
        Set<ConstraintViolation<Persona>> violations = validator.validate(persona);
        if (!violations.isEmpty()) {
            logger.info("Datos de entrada inválidos para crear persona: {}", violations);
            throw new jakarta.validation.ConstraintViolationException(violations);
        }
        return ResponseEntity.ok(servicioPersonas.crearPersona(persona));
    }

    @Operation(summary = "Obtener todas las personas", description = "Devuelve la lista completa de personas almacenadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de personas obtenida con éxito",
                    content = @Content(schema = @Schema(implementation = PersonaResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(examples = @ExampleObject(value = "\"Error al procesar la solicitud\"")))
    })
    @GetMapping
    public ResponseEntity<List<PersonaResponseDTO>> obtenerPersonas() throws Exception {
        return ResponseEntity.ok(servicioPersonas.obtenerPersonas());
    }

    @Operation(summary = "Obtener una persona por RUT", description = "Devuelve los detalles de una persona específica según su RUT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Persona encontrada",
                    content = @Content(schema = @Schema(implementation = PersonaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Persona no encontrada",
                    content = @Content(examples = @ExampleObject(value = "\"Persona no encontrada\""))),
            @ApiResponse(responseCode = "409", description = "RUT inválido",
                    content = @Content(examples = @ExampleObject(value = "\"RUT inválido\""))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(examples = @ExampleObject(value = "\"Error al procesar la solicitud\"")))
    })
    @GetMapping("/{rut}")
    public ResponseEntity<PersonaResponseDTO> obtenerPersona(@PathVariable String rut) throws Exception {
        if (!validadorRut.isValid(rut, null)) {
            logger.info("RUT inválido detectado: {}", rut);
            throw new jakarta.validation.ConstraintViolationException("RUT inválido", null);
        }
        PersonaResponseDTO persona = servicioPersonas.obtenerPersonaPorRut(rut);
        if (persona == null) {
            logger.info("Persona no encontrada para RUT: {}", rut);
            throw new IllegalArgumentException("Persona no encontrada");
        }
        return ResponseEntity.ok(persona);
    }

    @Operation(summary = "Actualizar una persona", description = "Actualiza los datos de una persona existente según su RUT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Persona actualizada con éxito",
                    content = @Content(schema = @Schema(implementation = PersonaResponseDTO.class))),
            @ApiResponse(responseCode = "202", description = "Operación guardada temporalmente debido a indisponibilidad de Firestore",
                    content = @Content(examples = @ExampleObject(value = "\"Operación guardada temporalmente\""))),
            @ApiResponse(responseCode = "409", description = "El RUT ya está registrado o es inválido",
                    content = @Content(examples = @ExampleObject(value = "\"El RUT ya está registrado\""))),
            @ApiResponse(responseCode = "404", description = "Persona no encontrada",
                    content = @Content(examples = @ExampleObject(value = "\"Persona no encontrada\""))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(examples = @ExampleObject(value = "\"Error al procesar la solicitud\"")))
    })
    @PutMapping("/{rut}")
    public ResponseEntity<PersonaResponseDTO> actualizarPersona(@PathVariable String rut, @RequestBody Persona persona) throws Exception {
        Set<ConstraintViolation<Persona>> violations = validator.validate(persona);
        if (!violations.isEmpty()) {
            logger.info("Datos de entrada inválidos para actualizar persona: {}", violations);
            throw new jakarta.validation.ConstraintViolationException(violations);
        }
        if (!validadorRut.isValid(rut, null)) {
            logger.info("RUT inválido detectado: {}", rut);
            throw new jakarta.validation.ConstraintViolationException("RUT inválido", null);
        }
        return ResponseEntity.ok(servicioPersonas.actualizarPersona(rut, persona));
    }

    @Operation(summary = "Eliminar una persona", description = "Elimina una persona de la base de datos según su RUT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Persona eliminada con éxito",
                    content = @Content(examples = @ExampleObject(value = "\"Persona eliminada con éxito\""))),
            @ApiResponse(responseCode = "202", description = "Operación guardada temporalmente debido a indisponibilidad de Firestore",
                    content = @Content(examples = @ExampleObject(value = "\"Operación guardada temporalmente\""))),
            @ApiResponse(responseCode = "404", description = "Persona no encontrada",
                    content = @Content(examples = @ExampleObject(value = "\"Persona no encontrada\""))),
            @ApiResponse(responseCode = "409", description = "RUT inválido",
                    content = @Content(examples = @ExampleObject(value = "\"RUT inválido\""))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(examples = @ExampleObject(value = "\"Error al procesar la solicitud\"")))
    })
    @DeleteMapping("/{rut}")
    public ResponseEntity<String> eliminarPersona(@PathVariable String rut) throws Exception {
        if (!validadorRut.isValid(rut, null)) {
            logger.info("RUT inválido detectado: {}", rut);
            throw new jakarta.validation.ConstraintViolationException("RUT inválido", null);
        }
        servicioPersonas.eliminarPersona(rut);
        return ResponseEntity.ok("Persona eliminada con éxito");
    }
}