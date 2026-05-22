package app.model.CapaControladores;

import app.model.CapaEntidades.SolicitudIntercambio;
import app.model.CapaPersistencia.PersistenciaSolicitud;
import app.model.CapaGestion.GestionSolicitudIntercambio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "http://localhost:5173")
public class SolicitudIntercambioController {

    @Autowired
    private PersistenciaSolicitud persistenciaSolicitud;

    @Autowired
    private GestionSolicitudIntercambio gestionSolicitud;

    // 1. MÉTODOS GET: LISTAR TODO (El método que te faltaba para /api/solicitudes)
    @GetMapping
    public List<SolicitudIntercambio> listarTodas() {
        return persistenciaSolicitud.cargar();
    }

    // 2. MÉTODOS GET: BUSCAR INDIVIDUAL (Añadido por trazabilidad para /api/solicitudes/SOL-001)
    @GetMapping("/{id}")
    public SolicitudIntercambio obtenerPorId(@PathVariable String id) {
        return persistenciaSolicitud.cargar().stream()
                .filter(s -> s.getIdSolicitudIntercambio().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada con el ID: " + id));
    }

    // 3. Endpoint para obtener las solicitudes enviadas por un usuario
    @GetMapping("/enviadas/{idEmisor}")
    public ResponseEntity<java.util.List<SolicitudIntercambio>> obtenerEnviadas(@PathVariable String idEmisor) {
        try {
            java.util.List<SolicitudIntercambio> enviadas = gestionSolicitud.obtenerSolicitudesEnviadas(idEmisor);
            return ResponseEntity.ok(enviadas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. Endpoint para cancelar una solicitud pendiente
    @PutMapping("/cancelar/{idSolicitud}")
    public ResponseEntity<String> cancelarSolicitud(@PathVariable String idSolicitud) {
        try {
            gestionSolicitud.cancelarSolicitud(idSolicitud);
            return ResponseEntity.ok("{\"mensaje\": \"Solicitud cancelada correctamente y créditos devueltos.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}