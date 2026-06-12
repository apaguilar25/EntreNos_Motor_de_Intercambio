package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.servicios.ServicioAdministrador;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestHeader("X-Usuario-Id") String idAdministrador, // Simulamos autenticación pasando el ID en el Header
            @RequestBody Map<String, String> request) {

        try {
            String idInfractor = request.get("idUsuarioInfractor");

            if (idInfractor == null || idInfractor.isEmpty()) {
                return ResponseEntity.badRequest().body("El ID del infractor es obligatorio.");
            }

            servicioAdministrador.validarReporteDeFraude(idAdministrador, idInfractor);
            return ResponseEntity.ok("Reporte de fraude validado exitosamente. Sanción aplicada si corresponde.");

        } catch (SecurityException e) {
            // Si el usuario que intenta hacer esto no es administrador
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}