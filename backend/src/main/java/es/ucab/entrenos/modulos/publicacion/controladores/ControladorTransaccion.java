package es.ucab.entrenos.modulos.publicacion.controladores;

import es.ucab.entrenos.modulos.publicacion.dto.CalificarRequestDTO;
import es.ucab.entrenos.modulos.publicacion.dtos.ConfirmacionTransaccionResponseDTO;
import es.ucab.entrenos.modulos.publicacion.dtos.ReportarIncidenciaRequestDTO;
import es.ucab.entrenos.modulos.publicacion.dtos.SolicitarCancelacionRequestDTO;
import es.ucab.entrenos.modulos.publicacion.dtos.ResponderCancelacionRequestDTO;
import es.ucab.entrenos.modulos.publicacion.modelos.Cancelacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Incidencia;
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/confirmar-demandante")
    public ResponseEntity<?> confirmarDemandante(@PathVariable String id) {
        try {
            ConfirmacionTransaccionResponseDTO respuesta = servicioPublicacion.confirmarDemandante(id);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/solicitar-cancelacion")
    public ResponseEntity<?> solicitarCancelacion(@PathVariable String id,
                                                   @RequestBody SolicitarCancelacionRequestDTO dto) {
        try {
            Cancelacion cancelacion = servicioPublicacion.solicitarCancelacion(id, dto.getIdUsuario(), dto.getMotivo());
            return ResponseEntity.ok(cancelacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/responder-cancelacion")
    public ResponseEntity<?> responderCancelacion(@PathVariable String id,
                                                   @RequestBody ResponderCancelacionRequestDTO dto) {
        try {
            Cancelacion cancelacion = servicioPublicacion.responderCancelacion(id,
                    dto.getIdUsuario(), dto.isAceptar());
            return ResponseEntity.ok(cancelacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/calificar")
    public ResponseEntity<?> calificar(@PathVariable String id,
                                        @RequestBody CalificarRequestDTO dto) {
        try {
            Transaccion t = servicioPublicacion.calificar(id, dto.getIdUsuario(),
                    dto.getCalificacion());
            return ResponseEntity.ok(t);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/incidencia")
    public ResponseEntity<?> obtenerIncidencia(@PathVariable String id) {
        return servicioPublicacion.obtenerIncidenciaPorTransaccion(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{id}/cancelar-reporte")
    public ResponseEntity<?> cancelarReporte(@PathVariable String id,
                                              @RequestBody ReportarIncidenciaRequestDTO dto) {
        try {
            Incidencia incidencia = servicioPublicacion.cancelarReporte(id,
                    dto.getIdUsuario(), dto.getDescripcion());
            return ResponseEntity.ok(incidencia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reportar-incidencia")
    public ResponseEntity<?> reportarIncidencia(@PathVariable String id,
                                                  @RequestBody ReportarIncidenciaRequestDTO dto) {
        try {
            Incidencia incidencia = servicioPublicacion.reportarIncidencia(id,
                    dto.getIdUsuario(), dto.getDescripcion(), dto.getUrlEvidencia(), dto.getFotosEvidencia());
            return ResponseEntity.ok(incidencia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/defender-incidencia")
    public ResponseEntity<?> defenderIncidencia(@PathVariable String id,
                                                  @RequestBody ReportarIncidenciaRequestDTO dto) {
        try {
            Incidencia incidencia = servicioPublicacion.defenderIncidencia(id,
                    dto.getIdUsuario(), dto.getDescripcion(), dto.getUrlEvidencia(), dto.getFotosEvidencia());
            return ResponseEntity.ok(incidencia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
