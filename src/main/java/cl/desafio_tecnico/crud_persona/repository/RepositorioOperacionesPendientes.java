package cl.desafio_tecnico.crud_persona.repository;

import cl.desafio_tecnico.crud_persona.model.OperacionPendiente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioOperacionesPendientes extends JpaRepository<OperacionPendiente, String> {
}