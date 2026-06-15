package es.ucab.entrenos.modulos.publicacion.repositorios;

import es.ucab.entrenos.modulos.publicacion.modelos.Solicitud;

import java.util.List;
import java.util.Optional;

public interface IRepositorioSolicitud {
    List<Solicitud> obtenerTodas();
    Optional<Solicitud> obtenerPorId(String idSolicitud);
    void guardar(Solicitud solicitud);
    void guardarTodas(List<Solicitud> solicitudes);
}
