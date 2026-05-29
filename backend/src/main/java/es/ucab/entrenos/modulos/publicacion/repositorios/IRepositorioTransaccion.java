package es.ucab.entrenos.modulos.publicacion.repositorios;

import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import java.util.List;
import java.util.Optional;
public interface IRepositorioTransaccion {
    List<Transaccion> obtenerTodas();
    Optional<Transaccion> obtenerPorId(String idTransaccion);
    void guardar(Transaccion transaccion);
    void guardarTodas(List<Transaccion> transacciones);
}
