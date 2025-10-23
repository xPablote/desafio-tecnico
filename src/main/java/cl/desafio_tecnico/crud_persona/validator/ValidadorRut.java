package cl.desafio_tecnico.crud_persona.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidadorRut implements ConstraintValidator<RutValido, String> {

    @Override
    public boolean isValid(String rut, ConstraintValidatorContext context) {
        if (rut == null || rut.isEmpty()) {
            return false;
        }

        // Eliminar puntos y guión
        rut = rut.replace(".", "").replace("-", "").toUpperCase();

        // Validar formato: al menos 2 caracteres, último carácter es dígito o 'K'
        if (rut.length() < 2) {
            return false;
        }
        char ultimoCaracter = rut.charAt(rut.length() - 1);
        if (!Character.isDigit(ultimoCaracter) && ultimoCaracter != 'K') {
            return false;
        }

        // Separar número y dígito verificador
        String numeroStr = rut.substring(0, rut.length() - 1);
        char digitoVerificador = ultimoCaracter;

        // Validar que el número solo contenga dígitos
        if (!numeroStr.matches("\\d+")) {
            return false;
        }

        // Calcular dígito verificador
        int suma = 0;
        int multiplicador = 2;
        for (int i = numeroStr.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(numeroStr.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        int dvCalculado = 11 - resto;

        char dvEsperado;
        if (dvCalculado == 11) {
            dvEsperado = '0';
        } else if (dvCalculado == 10) {
            dvEsperado = 'K';
        } else {
            dvEsperado = Character.forDigit(dvCalculado, 10);
        }

        return dvEsperado == digitoVerificador;
    }
}