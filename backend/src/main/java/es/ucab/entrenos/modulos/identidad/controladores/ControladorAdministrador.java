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

    @GetMapping("/incidencias/{id}")
    public ResponseEntity<Incidencia> obtenerIncidencia(@PathVariable String id) {
        return servicioAdministrador.obtenerIncidenciaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // --- GESTIÓN DE CORREOS PERMITIDOS ---
    @GetMapping("/correos")
    public ResponseEntity<?> listarCorreosPermitidos(@AuthenticationPrincipal Object principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(servicioAdministrador.listarCorreosPermitidos((String) principal));
    }

    @PostMapping("/correos")
    public ResponseEntity<?> agregarCorreoPermitido(@AuthenticationPrincipal Object principal, @RequestBody Map<String, String> request) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        servicioAdministrador.agregarCorreoPermitido((String) principal, request.get("correo"));
        return ResponseEntity.ok("Correo permitido agregado.");
    }

    @DeleteMapping("/correos/{correo}")
    public ResponseEntity<?> eliminarCorreoPermitido(@AuthenticationPrincipal Object principal, @PathVariable String correo) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        servicioAdministrador.eliminarCorreoPermitido((String) principal, correo);
        return ResponseEntity.ok("Correo permitido eliminado.");
    }

    // --- GESTIÓN DE USUARIOS ---
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(@AuthenticationPrincipal Object principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(servicioAdministrador.listarUsuarios((String) principal));
    }

    @PutMapping("/usuarios/{idUsuario}/creditos")
    public ResponseEntity<?> modificarCreditos(@AuthenticationPrincipal Object principal, @PathVariable String idUsuario, @RequestBody Map<String, Float> request) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Float nuevosCreditos = request.get("creditos");
        if (nuevosCreditos == null) return ResponseEntity.badRequest().body("Falta el campo 'creditos'");
        servicioAdministrador.modificarCreditosUsuario((String) principal, idUsuario, nuevosCreditos);
        return ResponseEntity.ok("Créditos modificados exitosamente.");
    }

    @PutMapping("/usuarios/{idUsuario}/perdonar-faltas")
    public ResponseEntity<?> perdonarFaltas(@AuthenticationPrincipal Object principal, @PathVariable String idUsuario) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        servicioAdministrador.perdonarFaltas((String) principal, idUsuario);
        return ResponseEntity.ok("Faltas perdonadas exitosamente.");
    }

    @PostMapping("/incidencias/{idIncidencia}/resolver")
    public ResponseEntity<?> resolverIncidencia(
            @AuthenticationPrincipal Object principal,
            @PathVariable String idIncidencia,
            @RequestBody Map<String, Object> request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acceso denegado: Token ausente o inválido.");
            }

            String idAdministrador = (String) principal;
            String idUsuarioGanador = (String) request.get("idUsuarioGanadorCreditos");
            Boolean sancionarOfertante = (Boolean) request.get("sancionarOfertante");
            Boolean sancionarDemandante = (Boolean) request.get("sancionarDemandante");

            if (sancionarOfertante == null) sancionarOfertante = false;
            if (sancionarDemandante == null) sancionarDemandante = false;

            servicioAdministrador.resolverIncidencia(idAdministrador, idIncidencia, idUsuarioGanador, sancionarOfertante, sancionarDemandante);
            return ResponseEntity.ok("Incidencia resuelta exitosamente.");

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
