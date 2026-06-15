package es.ucab.entrenos.modulos.publicacion.repositorios;

import es.ucab.entrenos.modulos.publicacion.modelos.MotivoCancelacion;
import java.util.List;
import java.util.Optional;

public interface IRepositorioMotivoCancelacion {
    List<MotivoCancelacion> obtenerTodos();
    Optional<MotivoCancelacion> obtenerPorId(String id);
}
