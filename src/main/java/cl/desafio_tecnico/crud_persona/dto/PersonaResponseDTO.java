package cl.desafio_tecnico.crud_persona.dto;

import cl.desafio_tecnico.crud_persona.model.Direccion;
import cl.desafio_tecnico.crud_persona.model.Persona;
import cl.desafio_tecnico.crud_persona.validator.RutValido;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonaResponseDTO {
    @NotBlank(message = "El RUT es obligatorio")
    @RutValido
    @JsonProperty("rut")
    @Schema(example = "19911121-3")
    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    @JsonProperty("nombre")
    @Schema(example = "Julia")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @JsonProperty("apellido")
    @Schema(example = "Sim")
    private String apellido;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @JsonFormat(pattern = "dd-MM-yyyy")
    @JsonProperty("fechaNacimiento")
    @Schema(example = "01-01-1990")
    private Timestamp fechaNacimiento;

    @JsonProperty("direccion")
    @Schema(example = "{\"calle\": \"Viva 123\", \"comuna\": \"Santiago\", \"region\": \"Metropolitana\"}")
    private Direccion direccion;

    public PersonaResponseDTO(Persona persona) {
        this.rut = persona.getRut();
        this.nombre = persona.getNombre();
        this.apellido = persona.getApellido();
        this.fechaNacimiento = persona.getFechaNacimiento();
        this.direccion = persona.getDireccion();
    }
}