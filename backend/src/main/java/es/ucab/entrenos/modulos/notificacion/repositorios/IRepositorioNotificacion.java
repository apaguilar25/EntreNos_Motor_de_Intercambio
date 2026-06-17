package es.ucab.entrenos.modulos.notificacion.repositorios;

import es.ucab.entrenos.modulos.notificacion.modelos.Notificacion;
import java.util.List;
import java.util.Optional;

public interface IRepositorioNotificacion {
    List<Notificacion> obtenerTodas();
    Optional<Notificacion> obtenerPorId(String idNotificacion);
    List<Notificacion> obtenerPorDestinatario(String idDestinatario);
    List<Notificacion> obtenerNoLeidas(String idDestinatario);
    void guardar(Notificacion notificacion);
    void guardarTodas(List<Notificacion> notificaciones);
    void marcarComoLeida(String idNotificacion);
    void eliminar(String idNotificacion);
    void eliminarPorReferencia(String idDestinatario, String idReferencia);
}
