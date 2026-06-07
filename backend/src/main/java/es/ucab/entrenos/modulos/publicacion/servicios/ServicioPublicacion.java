package es.ucab.entrenos.modulos.publicacion.servicios;

import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioPublicacion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class ServicioPublicacion {
    private final IRepositorioPublicacion repositorioPublicacion;
    private final IRepositorioTransaccion repositorioTransaccion;
    @Autowired
    public ServicioPublicacion(IRepositorioPublicacion repositorioPublicacion,
                               IRepositorioTransaccion repositorioTransaccion) {
        this.repositorioPublicacion = repositorioPublicacion;
        this.repositorioTransaccion = repositorioTransaccion;
    }
    // --- Servicios de Publicación ---
    public List<Publicacion> obtenerTodasLasPublicaciones() {
        return repositorioPublicacion.obtenerTodas();
    }
    public List<Publicacion> obtenerPublicacionesFiltradas(String tipo, String servicio) {
        return repositorioPublicacion.obtenerTodas().stream()
                .filter(p -> tipo == null || tipo.isEmpty() || p.getTipoPublicacion().equalsIgnoreCase(tipo))
                .filter(p -> servicio == null || servicio.isEmpty() ||
                        p.getNombreServicio().toLowerCase().contains(servicio.toLowerCase()) ||
                        p.getDescripcion().toLowerCase().contains(servicio.toLowerCase()))
                .collect(Collectors.toList());
    }
    public Publicacion crearPublicacion(Publicacion publicacion) {
        if (publicacion.getIdPublicacion() == null || publicacion.getIdPublicacion().isEmpty()) {
            publicacion.setIdPublicacion("PUB-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        publicacion.setDisponible(true);
        publicacion.setFechaCreacion(System.currentTimeMillis());
        repositorioPublicacion.guardar(publicacion);
        return publicacion;
    }
    public Optional<Publicacion> obtenerPublicacionPorId(String id) {
        return repositorioPublicacion.obtenerPorId(id);
    }
    public boolean eliminarPublicacion(String id) {
        List<Publicacion> todas = repositorioPublicacion.obtenerTodas();
        boolean removido = todas.removeIf(p -> p.getIdPublicacion().equalsIgnoreCase(id));
        if (removido) {
            repositorioPublicacion.guardarTodas(todas);
        }
        return removido;
    }
    // --- Servicios de Transacción ---
    public List<Transaccion> obtenerTodasLasTransacciones() {
        return repositorioTransaccion.obtenerTodas();
    }
    public Optional<Transaccion> obtenerTransaccionPorId(String id) {
        return repositorioTransaccion.obtenerPorId(id);
    }
    public Transaccion crearTransaccion(Transaccion transaccion) {
        if (transaccion.getIdTransaccion() == null || transaccion.getIdTransaccion().isEmpty()) {
            transaccion.setIdTransaccion("TX-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        transaccion.setFechaCreacion(System.currentTimeMillis());
        repositorioTransaccion.guardar(transaccion);
        return transaccion;
    }
    public Transaccion confirmarOfertante(String idTransaccion) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        t.confirmarEntregaOfertante();
        repositorioTransaccion.guardar(t);
        return t;
    }
    public Transaccion confirmarDemandante(String idTransaccion) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        t.confirmarRecepcionDemandante();
        repositorioTransaccion.guardar(t);
        return t;
    }
}
