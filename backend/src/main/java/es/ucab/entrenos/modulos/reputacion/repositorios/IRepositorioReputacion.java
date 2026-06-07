package es.ucab.entrenos.modulos.reputacion.repositorios;

import es.ucab.entrenos.modulos.reputacion.modelos.Resena;
import java.util.List;
import java.util.Optional;

public interface IRepositorioReputacion {
    List<Resena> obtenerTodas();
    Optional<Resena> obtenerPorId(String idResena);
    List<Resena> obtenerPorReceptor(String idUsuario);
    List<Resena> obtenerPorEmisor(String idUsuario);
    List<Resena> obtenerPorTransaccion(String idTransaccion);
    void guardar(Resena resena);
    void guardarTodas(List<Resena> resenas);
}
