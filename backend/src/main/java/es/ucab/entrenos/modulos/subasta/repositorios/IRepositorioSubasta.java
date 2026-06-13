package es.ucab.entrenos.modulos.subasta.repositorios;

import es.ucab.entrenos.modulos.subasta.modelos.Subasta;
import java.util.List;
import java.util.Optional;

public interface IRepositorioSubasta {
    List<Subasta> listarTodas();
    Optional<Subasta> buscarPorId(String idSubasta);

    void guardar(Subasta subasta);
}