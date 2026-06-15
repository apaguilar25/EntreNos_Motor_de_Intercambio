package es.ucab.entrenos.modulos.publicacion.controladores;

import es.ucab.entrenos.modulos.publicacion.dto.CancelarRequestDTO;
import es.ucab.entrenos.modulos.publicacion.dto.RespuestaSolicitudDTO;
import es.ucab.entrenos.modulos.publicacion.dto.SolicitudRequestDTO;
import es.ucab.entrenos.modulos.publicacion.modelos.Solicitud;
import es.ucab.entrenos.modulos.publicacion.servicios.ServicioSolicitud;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ControladorSolicitud {

    private final ServicioSolicitud servicioSolicitud;

    public ControladorSolicitud(ServicioSolicitud servicioSolicitud) {
        this.servicioSolicitud = servicioSolicitud;
    }

    @PostMapping("/publicaciones/{idPublicacion}/solicitar")
    public ResponseEntity<?> solicitar(@PathVariable String idPublicacion,
                                       @RequestBody SolicitudRequestDTO dto) {
        try {
            Solicitud solicitud = servicioSolicitud.solicitar(idPublicacion, dto.getIdUsuario());
            return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/solicitudes/{id}/responder")
    public ResponseEntity<?> responder(@PathVariable String id,
                                       @RequestBody RespuestaSolicitudDTO dto) {
        try {
            Solicitud solicitud = servicioSolicitud.responder(id, dto.getIdUsuario(), dto.isAceptar());
            return ResponseEntity.ok(solicitud);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<Solicitud> obtenerPorId(@PathVariable String id) {
        return servicioSolicitud.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/solicitudes/publicacion/{idPublicacion}")
    public ResponseEntity<List<Solicitud>> obtenerPorPublicacion(@PathVariable String idPublicacion) {
        return ResponseEntity.ok(servicioSolicitud.obtenerPorPublicacion(idPublicacion));
    }

    @GetMapping("/solicitudes/usuario/{idUsuario}")
    public ResponseEntity<List<Solicitud>> obtenerPorSolicitante(@PathVariable String idUsuario) {
        return ResponseEntity.ok(servicioSolicitud.obtenerPorSolicitante(idUsuario));
    }

    @PostMapping("/solicitudes/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable String id,
                                       @RequestBody CancelarRequestDTO dto) {
        try {
            Solicitud solicitud = servicioSolicitud.cancelar(id, dto.getIdUsuario());
            return ResponseEntity.ok(solicitud);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
