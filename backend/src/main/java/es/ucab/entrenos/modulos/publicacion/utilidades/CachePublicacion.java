package es.ucab.entrenos.modulos.publicacion.utilidades;

import es.ucab.entrenos.modulos.publicacion.dtos.PublicacionResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CachePublicacion {

    private static final Logger log = LoggerFactory.getLogger(CachePublicacion.class);
    private volatile List<PublicacionResponseDTO> cache = Collections.emptyList();

    public void actualizar(List<PublicacionResponseDTO> todas) {
        List<PublicacionResponseDTO> nuevo = todas.stream()
                .sorted(Comparator.comparingDouble(
                        PublicacionResponseDTO::getReputacionUsuario)
                .reversed())
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        Collections::unmodifiableList));
        this.cache = nuevo;
        log.debug("CachePublicacion actualizada con {} publicaciones", nuevo.size());
    }

    public List<PublicacionResponseDTO> getTodas() {
        return cache;
    }

    public void refrescar(List<PublicacionResponseDTO> todas) {
        actualizar(todas);
    }
}
