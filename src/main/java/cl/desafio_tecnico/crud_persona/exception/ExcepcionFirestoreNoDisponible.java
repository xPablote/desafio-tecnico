package cl.desafio_tecnico.crud_persona.exception;

public class ExcepcionFirestoreNoDisponible extends Exception {
    public ExcepcionFirestoreNoDisponible(String mensaje) {
        super(mensaje);
    }
}