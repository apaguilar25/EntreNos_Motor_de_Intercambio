package es.ucab.entrenos.modulos.publicacion.repositorios;

import es.ucab.entrenos.modulos.publicacion.modelos.Incidencia;
import java.util.List;
import java.util.Optional;

public interface IRepositorioIncidencia {
    List<Incidencia> obtenerTodas();
    Optional<Incidencia> obtenerPorId(String idIncidencia);
    void guardar(Incidencia incidencia);
}
