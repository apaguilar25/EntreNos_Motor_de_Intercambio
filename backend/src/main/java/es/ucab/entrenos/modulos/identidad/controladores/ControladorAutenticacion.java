package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.dtos.PerfilUsuarioResponseDTO;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioAutenticacion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    /**
     * Endpoint público para iniciar sesión.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody Map<String, String> credenciales) {
        try {
            String correo = credenciales.get("correoElectronico");
            String contrasena = credenciales.get("contrasena");

            // Delegamos la validación al servicio
            Usuario usuarioAutenticado = servicioAutenticacion.login(correo, contrasena);

            // Si es exitoso, mapeamos la entidad al DTO que oculta datos sensibles y muestra el estado
            PerfilUsuarioResponseDTO respuestaFront = new PerfilUsuarioResponseDTO(usuarioAutenticado);

            // Temporalmente (sin JWT), devolvemos el DTO completo con estado 200 OK
            return ResponseEntity.ok(respuestaFront);

        } catch (SecurityException e) {
            // Error genérico para credenciales inválidas (OWASP) o bloqueos
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // Solicitud mal formada (faltó el correo o la contraseña en el JSON)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Error inesperado en el servidor
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error interno durante la autenticación."));
        }
    }
}