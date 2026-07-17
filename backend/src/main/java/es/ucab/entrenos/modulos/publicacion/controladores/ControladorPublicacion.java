package es.ucab.entrenos.modulos.publicacion.controladores;

import es.ucab.entrenos.modulos.publicacion.dtos.ConfirmacionTransaccionResponseDTO;
import es.ucab.entrenos.modulos.publicacion.dtos.PublicacionResponseDTO;
import es.ucab.entrenos.modulos.publicacion.dto.RecomendacionDTO;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.servicios.ServicioPublicacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
@RestController
@RequestMapping("/api/publicaciones")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorPublicacion {
    private final ServicioPublicacion servicioPublicacion;
    @Autowired
    public ControladorPublicacion(ServicioPublicacion servicioPublicacion) {
        this.servicioPublicacion = servicioPublicacion;
    }
    // --- SSE Muro en Tiempo Real ---
    @GetMapping("/stream")
    public SseEmitter streamMuro() {
        return servicioPublicacion.suscribirMuro();
    }

    // --- Endpoints de Publicación ---
    @GetMapping
    public ResponseEntity<List<PublicacionResponseDTO>> obtenerPublicaciones(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String servicio) {
        return ResponseEntity.ok(
                servicioPublicacion.obtenerPublicacionesFiltradas(tipo, servicio));
    }
    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponseDTO> obtenerPorId(@PathVariable String id) {
        return servicioPublicacion.obtenerPublicacionPorId(id)
                .map(servicioPublicacion::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    @PostMapping
    public ResponseEntity<PublicacionResponseDTO> crear(@RequestBody es.ucab.entrenos.modulos.publicacion.modelos.Publicacion publicacion) {
        PublicacionResponseDTO nueva = servicioPublicacion.crearPublicacion(publicacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (servicioPublicacion.eliminarPublicacion(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<PublicacionResponseDTO>> obtenerPorUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(servicioPublicacion.obtenerPublicacionesPorUsuario(idUsuario));
    }

    // --- HU6: Endpoint de Recomendadas ---
    @GetMapping("/recomendadas/{idUsuario}")
    public ResponseEntity<List<RecomendacionDTO>> recomendadas(
            @PathVariable String idUsuario) {
        return ResponseEntity.ok(
                servicioPublicacion.obtenerRecomendadas(idUsuario));
    }

    @PostMapping("/recomendadas/{idUsuario}/adoptar/{idPublicacion}")
    public ResponseEntity<?> adoptar(@PathVariable String idUsuario,
                                     @PathVariable String idPublicacion) {
        try {
            List<RecomendacionDTO> actualizadas = servicioPublicacion.adoptarRecomendacion(idUsuario, idPublicacion);
            return ResponseEntity.ok(actualizadas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // --- HU3: Calificaciones de una publicación ---
    @GetMapping("/{id}/calificaciones")
    public ResponseEntity<List<Transaccion>> calificaciones(
            @PathVariable String id) {
        return ResponseEntity.ok(
                servicioPublicacion.obtenerCalificacionesPorPublicacion(id));
    }
}
