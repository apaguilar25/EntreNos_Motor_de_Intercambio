package es.ucab.entrenos.modulos.subasta.controladores;

import es.ucab.entrenos.modulos.subasta.modelos.EstadoFisico;
import es.ucab.entrenos.modulos.subasta.modelos.Subasta;
import es.ucab.entrenos.modulos.subasta.servicios.ServicioSubasta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import es.ucab.entrenos.modulos.subasta.dtos.SubastaDetalleDTO;

@RestController
@RequestMapping("/api/subastas")
public class ControladorSubasta {

    private final ServicioSubasta servicioSubasta;

    public ControladorSubasta(ServicioSubasta servicioSubasta) {
        this.servicioSubasta = servicioSubasta;
    }

    @PostMapping
    public ResponseEntity<Subasta> crearSubasta(@AuthenticationPrincipal String idPropietario, @RequestBody Map<String, Object> payload) {
        String nombreActivo = (String) payload.get("nombreActivo");
        String descripcion = (String) payload.get("descripcion");
        EstadoFisico estadoFisico = EstadoFisico.valueOf((String) payload.get("estadoFisico"));
        List<String> imagenesUrls = (List<String>) payload.get("imagenesUrls");
        int diasDuracion = (Integer) payload.getOrDefault("diasDuracion", 7);
        LocalDateTime fechaCierre = LocalDateTime.now().plusDays(diasDuracion);

        Subasta nueva = servicioSubasta.crearSubasta(idPropietario, nombreActivo, descripcion, estadoFisico, imagenesUrls, fechaCierre);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Subasta>> listarSubastasActivas() {
        return ResponseEntity.ok(servicioSubasta.listarSubastasActivas());
    }

    @GetMapping("/mis-subastas")
    public ResponseEntity<List<Subasta>> listarMisSubastas(@AuthenticationPrincipal String idPropietario) {
        return ResponseEntity.ok(servicioSubasta.listarSubastasPorPropietario(idPropietario));
    }

    @GetMapping("/{idSubasta}")
    public ResponseEntity<?> obtenerDetalleSubasta(@PathVariable String idSubasta) {
        try {
            SubastaDetalleDTO detalle = servicioSubasta.obtenerDetalleSubasta(idSubasta);
            return ResponseEntity.ok(detalle);
        } catch (IllegalArgumentException e) {
            // Si la subasta no existe, devolvemos un 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{idSubasta}/ofertar")
    public ResponseEntity<Subasta> hacerOferta(@PathVariable String idSubasta, @RequestBody es.ucab.entrenos.modulos.subasta.modelos.Propuesta propuesta) {
        Subasta subasta = servicioSubasta.hacerOferta(idSubasta, propuesta);
        return ResponseEntity.status(HttpStatus.CREATED).body(subasta);
    }

    @PostMapping("/{idSubasta}/ganador/{idPropuesta}")
    public ResponseEntity<?> adjudicarGanador(@AuthenticationPrincipal String idPropietario, @PathVariable String idSubasta, @PathVariable String idPropuesta) {
        try {
            return ResponseEntity.ok(servicioSubasta.adjudicarGanador(idPropietario, idSubasta, idPropuesta));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{idSubasta}/puja")
    public ResponseEntity<?> retirarPuja(@AuthenticationPrincipal String idUsuario, @PathVariable String idSubasta) {
        try {
            servicioSubasta.retirarPuja(idSubasta, idUsuario);
            return ResponseEntity.ok(Map.of("mensaje", "Tu puja ha sido retirada exitosamente."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al retirar la puja."));
        }
    }

    @PostMapping("/{idSubasta}/cancelar")
    public ResponseEntity<?> cancelarSubasta(@AuthenticationPrincipal String idPropietario, @PathVariable String idSubasta) {
        try {
            servicioSubasta.cancelarSubastaManual(idPropietario, idSubasta);
            return ResponseEntity.ok(Map.of("mensaje", "La subasta ha sido cancelada exitosamente."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{idSubasta}")
    public ResponseEntity<?> modificarSubasta(@AuthenticationPrincipal String idPropietario, @PathVariable String idSubasta, @RequestBody Map<String, String> payload) {
        try {
            String nuevaDescripcion = payload.get("descripcion");
            servicioSubasta.modificarSubasta(idPropietario, idSubasta, nuevaDescripcion);
            return ResponseEntity.ok(Map.of("mensaje", "La subasta ha sido modificada exitosamente."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{idSubasta}")
    public ResponseEntity<?> eliminarSubasta(@AuthenticationPrincipal String idPropietario, @PathVariable String idSubasta) {
        try {
            servicioSubasta.cancelarSubastaManual(idPropietario, idSubasta);
            return ResponseEntity.ok(Map.of("mensaje", "La subasta ha sido eliminada exitosamente."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}