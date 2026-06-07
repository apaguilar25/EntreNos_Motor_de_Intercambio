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
        // Al encender el servidor, leemos el JSON UNA SOLA VEZ y lo guardamos en RAM
        this.cacheHabilidades = repositorioHabilidad.listarTodas();
    }

    public List<Habilidad> obtenerTodas() {
        // Devolvemos la memoria RAM, es 100 veces más rápido que leer el disco
        return this.cacheHabilidades;
    }

    public Optional<Habilidad> buscarPorId(String id) {
        return cacheHabilidades.stream()
                .filter(h -> h.getId().equals(id))
                .findFirst();
    }
}