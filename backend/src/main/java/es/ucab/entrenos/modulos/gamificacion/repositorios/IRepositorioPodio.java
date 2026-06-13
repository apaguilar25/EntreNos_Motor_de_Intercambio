package es.ucab.entrenos.modulos.gamificacion.repositorios;

import es.ucab.entrenos.modulos.gamificacion.modelos.PodioSemanal;
import java.util.Optional;

public interface IRepositorioPodio {
    Optional<PodioSemanal> obtenerActual();
    void guardar(PodioSemanal podio);
}
