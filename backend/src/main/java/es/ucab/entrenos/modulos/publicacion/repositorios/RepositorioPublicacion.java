package es.ucab.entrenos.modulos.publicacion.repositorios;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import org.springframework.stereotype.Repository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Repository
public class RepositorioPublicacion implements IRepositorioPublicacion {
    private final File archivo = new File("data/publicaciones.json");
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public List<Publicacion> obtenerTodas() {
        if (!archivo.exists() || archivo.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(archivo, new TypeReference<List<Publicacion>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer publicaciones.json: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    @Override
    public Optional<Publicacion> obtenerPorId(String idPublicacion) {
        return obtenerTodas().stream()
                .filter(p -> p.getIdPublicacion().equalsIgnoreCase(idPublicacion))
                .findFirst();
    }
    @Override
    public void guardar(Publicacion publicacion) {
        List<Publicacion> todas = obtenerTodas();
        todas.removeIf(p -> p.getIdPublicacion().equalsIgnoreCase(publicacion.getIdPublicacion()));
        todas.add(publicacion);
        guardarTodas(todas);
    }
    @Override
    public void guardarTodas(List<Publicacion> publicaciones) {
        try {
            // Asegurar que la carpeta exista
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, publicaciones);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en publicaciones.json", e);
        }
    }
}
