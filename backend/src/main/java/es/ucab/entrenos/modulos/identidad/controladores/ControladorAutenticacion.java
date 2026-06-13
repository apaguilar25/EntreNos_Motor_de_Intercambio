package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.dtos.PerfilUsuarioResponseDTO;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioAutenticacion;
import es.ucab.entrenos.nucleo.seguridad.jwt.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;
    private final JwtUtil jwtUtil; // <-- INYECTADO PARA SOLUCIONAR EL ERROR

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion, JwtUtil jwtUtil) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody Map<String, String> credenciales) {
        try {
            String correo = credenciales.get("correoElectronico");
            String contrasena = credenciales.get("contrasena");

            // 1. Validamos credenciales usando tu servicio core
            Usuario usuarioAutenticado = servicioAutenticacion.login(correo, contrasena);

            // 2. Si pasa, generamos el Token Criptográfico (HU7)
            String token = jwtUtil.generarToken(usuarioAutenticado);
            PerfilUsuarioResponseDTO perfil = new PerfilUsuarioResponseDTO(usuarioAutenticado);

            // 3. Devolvemos el Token y la vista segura de datos (HU1)
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Autenticación exitosa",
                    "token", token,
                    "usuario", perfil
            ));

        } catch (SecurityException e) {
            // Contraseña incorrecta o usuario no encontrado
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            // Cuenta bloqueada por fuerza bruta (24h) o suspendida por fraude
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // Datos nulos o mal formateados
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}