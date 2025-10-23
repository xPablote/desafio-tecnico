package cl.desafio_tecnico.crud_persona.util;

import java.time.LocalDate;
import java.time.Period;

public class UtilidadesPersona {

    public static int calcularEdad(String fechaNacimiento) {
        if (fechaNacimiento == null) return 0;
        LocalDate fechaNac = LocalDate.parse(fechaNacimiento);
        return Period.between(fechaNac, LocalDate.now()).getYears();
    }
}