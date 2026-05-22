package app.model.CapaControladores;

import app.model.CapaEntidades.SolicitudIntercambio;
import app.model.CapaPersistencia.PersistenciaSolicitud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "*") // Permite la conexión limpia con el frontend (Vite)
public class SolicitudIntercambioController {

    @Autowired
    private PersistenciaSolicitud persistenciaSolicitud;

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
}