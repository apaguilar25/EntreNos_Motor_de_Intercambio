package es.ucab.entrenos.modulos.gamificacion.repositorios;

import es.ucab.entrenos.modulos.gamificacion.modelos.LogroDesbloqueado;
import java.util.List;
import java.util.Optional;

public interface IRepositorioLogroDesbloqueado {
    List<LogroDesbloqueado> obtenerTodos();
    Optional<LogroDesbloqueado> obtenerPorId(String id);
    List<LogroDesbloqueado> obtenerPorUsuario(String idUsuario);
    void guardar(LogroDesbloqueado logroDesbloqueado);
    void guardarTodas(List<LogroDesbloqueado> logrosDesbloqueados);
}
