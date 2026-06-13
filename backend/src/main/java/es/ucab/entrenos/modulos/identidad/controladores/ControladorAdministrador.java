package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.servicios.ServicioAdministrador;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class ControladorAdministrador {

    private final ServicioAdministrador servicioAdministrador;

    public ControladorAdministrador(ServicioAdministrador servicioAdministrador) {
        this.servicioAdministrador = servicioAdministrador;
    }

    /**
     * Endpoint para que el Admin valide un reporte de fraude.
     * POST /api/admin/sanciones/fraude
     */
    @PostMapping("/sanciones/fraude")
    public ResponseEntity<?> validarReporteFraude(
            @AuthenticationPrincipal Object principal, // <-- Spring inyecta automáticamente el ID extraído del Token
            @RequestBody Map<String, String> request) {
        try {
            // Validación defensiva para evitar la advertencia de IntelliJ
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acceso denegado: Token ausente o inválido.");
            }

            String idAdministrador = (String) principal;
            String idInfractor = request.get("idUsuarioInfractor");

            // Ejecutamos la lógica de negocio
            servicioAdministrador.validarReporteDeFraude(idAdministrador, idInfractor);
            return ResponseEntity.ok("Sanción aplicada exitosamente.");

        } catch (SecurityException e) {
            // Si el usuario autenticado no posee el rol de ADMINISTRADOR
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Si los IDs están vacíos, nulos o el infractor no existe
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}