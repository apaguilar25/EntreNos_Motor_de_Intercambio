package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.servicios.ServicioAdministrador;
import es.ucab.entrenos.modulos.publicacion.modelos.Incidencia;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class ControladorAdministrador {

    private final ServicioAdministrador servicioAdministrador;

    public ControladorAdministrador(ServicioAdministrador servicioAdministrador) {
        this.servicioAdministrador = servicioAdministrador;
    }

    @GetMapping("/incidencias")
    public ResponseEntity<?> listarIncidencias() {
        List<Incidencia> incidencias = servicioAdministrador.listarIncidencias();
        return ResponseEntity.ok(incidencias);
    }

    @PostMapping("/sanciones/fraude")
    public ResponseEntity<?> validarReporteFraude(
            @AuthenticationPrincipal Object principal,
            @RequestBody Map<String, String> request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acceso denegado: Token ausente o inválido.");
            }

            String idAdministrador = (String) principal;
            String idInfractor = request.get("idUsuarioInfractor");
            String idTransaccion = request.get("idTransaccion");

            servicioAdministrador.validarReporteDeFraude(idAdministrador, idInfractor, idTransaccion);
            return ResponseEntity.ok("Sanción aplicada exitosamente.");

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
