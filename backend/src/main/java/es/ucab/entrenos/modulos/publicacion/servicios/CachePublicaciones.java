package es.ucab.entrenos.modulos.publicacion.servicios;

import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class CachePublicaciones {

    private static final Logger log = LoggerFactory.getLogger(CachePublicaciones.class);
    private static final int MAX_SIZE = 10;

    private final ConcurrentHashMap<String, Publicacion> porId = new ConcurrentHashMap<>();
    private volatile List<Publicacion> recientes = Collections.emptyList();

    public void actualizar(List<Publicacion> todas) {
        List<Publicacion> top = todas.stream()
                .sorted(Comparator.comparingLong(Publicacion::getFechaCreacion).reversed())
                .limit(MAX_SIZE)
                .collect(Collectors.toList());
        porId.clear();
        top.forEach(p -> porId.put(p.getIdPublicacion(), p));
        this.recientes = Collections.unmodifiableList(top);
        log.info("Caché actualizada con {} publicaciones", top.size());
    }

    public List<Publicacion> getRecientes() {
        return recientes;
    }

    public void invalidar(String idPublicacion) {
        porId.remove(idPublicacion);
        recientes = recientes.stream()
                .filter(p -> !p.getIdPublicacion().equals(idPublicacion))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(), Collections::unmodifiableList));
    }

    public void refrescar(List<Publicacion> todas) {
        actualizar(todas);
    }

    public boolean contiene(String idPublicacion) {
        return porId.containsKey(idPublicacion);
    }
}
