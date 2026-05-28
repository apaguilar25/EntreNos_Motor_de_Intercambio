package es.ucab.entrenos.modulos.publicacion.repositorios;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import java.util.List;
import java.util.Optional;
public interface IRepositorioPublicacion {
    List<Publicacion> obtenerTodas();
    Optional<Publicacion> obtenerPorId(String idPublicacion);
    void guardar(Publicacion publicacion);
    void guardarTodas(List<Publicacion> publicaciones);
}
