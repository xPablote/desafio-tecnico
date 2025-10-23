package cl.desafio_tecnico.crud_persona.exception;

public class ExcepcionRutInmutable extends RuntimeException {
    public ExcepcionRutInmutable(String message) {
        super(message);
    }
}