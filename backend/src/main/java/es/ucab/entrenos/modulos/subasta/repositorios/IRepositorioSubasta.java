package es.ucab.entrenos.modulos.subasta.repositorios;

import es.ucab.entrenos.modulos.subasta.modelos.Subasta;
import java.util.List;
import java.util.Optional;

public interface IRepositorioSubasta {
    void guardar(Subasta subasta);
    Optional<Subasta> buscarPorId(String id);
    List<Subasta> listarTodas();
}