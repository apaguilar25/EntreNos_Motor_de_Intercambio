package es.ucab.entrenos.modulos.publicacion.controladores;

import es.ucab.entrenos.modulos.publicacion.dtos.ConfirmacionTransaccionResponseDTO;
import es.ucab.entrenos.modulos.publicacion.modelos.Incidencia;
import es.ucab.entrenos.modulos.publicacion.modelos.Cancelacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.servicios.ServicioTransaccion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorTransaccion {
    private final ServicioTransaccion servicioTransaccion;

    public ControladorTransaccion(ServicioTransaccion servicioTransaccion) {
        this.servicioTransaccion = servicioTransaccion;
    }

    @GetMapping
    public ResponseEntity<List<Transaccion>> obtenerTodas() {
        return ResponseEntity.ok(servicioTransaccion.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaccion> obtenerPorId(@PathVariable String id) {
        return servicioTransaccion.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Transaccion> crear(@RequestBody Transaccion transaccion) {
        Transaccion nueva = servicioTransaccion.crear(transaccion);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PostMapping("/{id}/confirmar-ofertante")
    public ResponseEntity<?> confirmarOfertante(@PathVariable String id) {
        try {
            ConfirmacionTransaccionResponseDTO respuesta = servicioTransaccion.confirmarOfertante(id);
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
            ConfirmacionTransaccionResponseDTO respuesta = servicioTransaccion.confirmarDemandante(id);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/calificar")
    public ResponseEntity<?> calificar(@PathVariable String id,
                                        @RequestParam String idUsuario,
                                        @RequestParam int calificacion) {
        try {
            Transaccion t = servicioTransaccion.calificar(id, idUsuario, calificacion);
            return ResponseEntity.ok(t);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/incidencia")
    public ResponseEntity<?> reportarIncidencia(@PathVariable String id,
                                                 @RequestParam String idUsuario,
                                                 @RequestParam String descripcion,
                                                 @RequestParam(required = false) String urlEvidencia) {
        try {
            Incidencia incidencia = servicioTransaccion.reportarIncidencia(id, idUsuario, descripcion, urlEvidencia);
            return ResponseEntity.status(HttpStatus.CREATED).body(incidencia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/defender")
    public ResponseEntity<?> defenderIncidencia(@PathVariable String id,
                                                 @RequestParam String idUsuario,
                                                 @RequestParam String descripcion,
                                                 @RequestParam(required = false) String urlEvidencia) {
        try {
            Incidencia incidencia = servicioTransaccion.defenderIncidencia(id, idUsuario, descripcion, urlEvidencia);
            return ResponseEntity.ok(incidencia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> solicitarCancelacion(@PathVariable String id,
                                                   @RequestParam String idUsuario,
                                                   @RequestParam String motivo) {
        try {
            Cancelacion cancelacion = servicioTransaccion.solicitarCancelacion(id, idUsuario, motivo);
            return ResponseEntity.status(HttpStatus.CREATED).body(cancelacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/responder-cancelacion/{aceptar}")
    public ResponseEntity<?> responderCancelacion(@PathVariable String id,
                                                   @RequestParam String idUsuario,
                                                   @PathVariable boolean aceptar) {
        try {
            Cancelacion cancelacion = servicioTransaccion.responderCancelacion(id, idUsuario, aceptar);
            return ResponseEntity.ok(cancelacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
