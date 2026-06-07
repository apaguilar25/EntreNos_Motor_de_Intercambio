package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.repositorios.RepositorioHabilidad;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioHabilidad {

    private final RepositorioHabilidad repositorioHabilidad;
    private final List<Habilidad> cacheHabilidades;

    public ServicioHabilidad(RepositorioHabilidad repositorioHabilidad) {
        this.repositorioHabilidad = repositorioHabilidad;
        // Leemos el disco duro UNA SOLA VEZ y guardamos en RAM
        this.cacheHabilidades = repositorioHabilidad.listarTodas();
    }

    public List<Habilidad> obtenerTodas() {
        // Devolvemos la lista desde la RAM (ultra rápido)
        return this.cacheHabilidades;
    }

    public Optional<Habilidad> buscarPorId(String id) {
        return cacheHabilidades.stream()
                .filter(h -> h.getId().equals(id))
                .findFirst();
    }
}