package cl.desafio_tecnico.crud_persona.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER}) // Donde se puede usar (campos y parámetros)
@Retention(RetentionPolicy.RUNTIME) // Accesible en tiempo de ejecución
@Constraint(validatedBy = ValidadorRut.class) // Clase que implementará la lógica
public @interface RutValido {
    // 1. Mensaje de error por defecto
    String message() default "El RUT es inválido (formato o dígito verificador).";

    // 2. Grupos de validación
    Class<?>[] groups() default {};

    // 3. Payload
    Class<? extends Payload>[] payload() default {};
}