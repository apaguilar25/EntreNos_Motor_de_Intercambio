package es.ucab.entrenos.modulos.gamificacion.repositorios;

import es.ucab.entrenos.modulos.gamificacion.modelos.Logro;
import java.util.List;
import java.util.Optional;

public interface IRepositorioLogro {
    List<Logro> obtenerTodos();
    Optional<Logro> obtenerPorId(String idLogro);
    void guardar(Logro logro);
    void guardarTodas(List<Logro> logros);
}
