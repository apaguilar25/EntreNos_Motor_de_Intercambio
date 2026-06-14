package es.ucab.entrenos.modulos.publicacion.controladores;

import es.ucab.entrenos.modulos.publicacion.dto.CalificarRequestDTO;
import es.ucab.entrenos.modulos.publicacion.dtos.CancelarTransaccionRequestDTO;
import es.ucab.entrenos.modulos.publicacion.dtos.ConfirmacionTransaccionResponseDTO;
import es.ucab.entrenos.modulos.publicacion.dtos.ReportarIncidenciaRequestDTO;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.servicios.ServicioPublicacion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/transacciones")
public class ControladorTransaccion {
    private final ServicioPublicacion servicioPublicacion;

    public ControladorTransaccion(ServicioPublicacion servicioPublicacion) {
        this.servicioPublicacion = servicioPublicacion;
    }

    @GetMapping
    public ResponseEntity<List<Transaccion>> obtenerTodas() {
        return ResponseEntity.ok(servicioPublicacion.obtenerTodasLasTransacciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaccion> obtenerPorId(@PathVariable String id) {
        return servicioPublicacion.obtenerTransaccionPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Transaccion> crear(@RequestBody Transaccion transaccion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioPublicacion.crearTransaccion(transaccion));
    }

    @PostMapping("/{id}/confirmar-ofertante")
    public ResponseEntity<?> confirmarOfertante(@PathVariable String id) {
        try {
            ConfirmacionTransaccionResponseDTO respuesta = servicioPublicacion.confirmarOfertante(id);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirmar-demandante")
    public ResponseEntity<?> confirmarDemandante(@PathVariable String id) {
        try {
            ConfirmacionTransaccionResponseDTO respuesta = servicioPublicacion.confirmarDemandante(id);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/calificar")
    public ResponseEntity<?> calificar(@PathVariable String id,
                                        @RequestBody CalificarRequestDTO dto) {
        try {
            Transaccion t = servicioPublicacion.calificar(id, dto.getIdUsuario(),
                    dto.getCalificacion(), dto.getComentario());
            return ResponseEntity.ok(t);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reportar-incidencia")
    public ResponseEntity<?> reportarIncidencia(@PathVariable String id,
                                                 @RequestBody ReportarIncidenciaRequestDTO dto) {
        try {
            Transaccion t = servicioPublicacion.reportarIncidencia(id,
                    dto.getDescripcion(), dto.getUrlEvidencia());
            return ResponseEntity.ok(t);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable String id,
                                       @RequestBody CancelarTransaccionRequestDTO dto) {
        try {
            Transaccion t = servicioPublicacion.cancelarTransaccion(id,
                    dto.getIdUsuario(), dto.getMotivo());
            return ResponseEntity.ok(t);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
