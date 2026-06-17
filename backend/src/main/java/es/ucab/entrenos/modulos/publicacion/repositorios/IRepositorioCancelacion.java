package es.ucab.entrenos.modulos.publicacion.repositorios;

import es.ucab.entrenos.modulos.publicacion.modelos.Cancelacion;

import java.util.List;
import java.util.Optional;

public interface IRepositorioCancelacion {
    List<Cancelacion> obtenerTodas();
    Optional<Cancelacion> obtenerPorId(String idCancelacion);
    void guardar(Cancelacion cancelacion);
    void guardarTodas(List<Cancelacion> cancelaciones);
}
