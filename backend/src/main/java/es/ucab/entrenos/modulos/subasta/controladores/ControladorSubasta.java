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

    @PostMapping("/{idSubasta}/ganador/{idPropuesta}")
    public ResponseEntity<?> adjudicarGanador(@AuthenticationPrincipal String idPropietario, @PathVariable String idSubasta, @PathVariable String idPropuesta) {
        return ResponseEntity.ok(servicioSubasta.adjudicarGanador(idPropietario, idSubasta, idPropuesta));
    }

    @PostMapping("/{idSubasta}/cancelar")
    public ResponseEntity<?> cancelarSubasta(@AuthenticationPrincipal String idPropietario, @PathVariable String idSubasta) {
        servicioSubasta.cancelarSubastaManual(idPropietario, idSubasta);
        return ResponseEntity.ok(Map.of("mensaje", "La subasta ha sido cancelada exitosamente."));
    }
}