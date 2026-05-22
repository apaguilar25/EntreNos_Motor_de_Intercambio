package app.model.CapaControladores;

import app.model.CapaEntidades.SolicitudIntercambio;
import app.model.CapaEntidades.EstadoSolicitudIntercambio;
import app.model.CapaGestion.GestionSolicitudIntercambio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "http://localhost:3000")
public class SolicitudIntercambioController {

    @Autowired
    private GestionSolicitudIntercambio gestionSolicitud;

    // 1. Endpoint para proponer un intercambio (React enviará un JSON en el Body)
    @PostMapping("/proponer")
    public ResponseEntity<String> proponerIntercambio(@RequestBody SolicitudIntercambio p) {
        try {
            gestionSolicitud.registrarSolicitud(
                    p.getIdEmisor(),
                    p.getIdReceptor(),
                    p.getNombreServicio(),
                    p.getPrecioCreditos(),
                    p.getDescripcionServicio()
            );
            return ResponseEntity.ok("{\"mensaje\": \"Propuesta enviada y notificación generada con éxito.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // 2. Endpoint para Aceptar o Rechazar desde los botones de React
    // Ejemplo: PUT http://localhost:8080/api/solicitudes/actualizar-estado?id=123&estado=ACEPTADA
    @PutMapping("/actualizar-estado")
    public ResponseEntity<String> actualizarEstado(
            @RequestParam String id,
            @RequestParam EstadoSolicitudIntercambio estado) {
        try {
            gestionSolicitud.actualizarEstado(id, estado);
            return ResponseEntity.ok("{\"mensaje\": \"Estado de la solicitud actualizado a " + estado + ".\"}");
        } catch (IllegalStateException e) {
            // Captura si se intenta modificar una solicitud que ya no está PENDIENTE
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"Error interno del servidor.\"}");
        }
    }
}