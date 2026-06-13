package es.ucab.entrenos.nucleo.excepciones;

import es.ucab.entrenos.modulos.identidad.excepciones.ConcurrenciaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Interceptor global de excepciones.
 * Atrapa los errores lanzados por los Servicios o Modelos y los transforma
 * automáticamente en respuestas JSON con el código HTTP correcto.
 */
@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    // Errores de validación o datos faltantes -> HTTP 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> manejarArgumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    // Errores de lógica de negocio (ej. "La subasta no está activa") -> HTTP 400 Bad Request
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> manejarEstadoInvalido(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    // Errores de permisos y baneos -> HTTP 403 Forbidden (Prohibido)
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> manejarErrorSeguridad(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
    }

    // Nuestro famoso error de sobreescritura simultánea -> HTTP 409 Conflict
    @ExceptionHandler(ConcurrenciaException.class)
    public ResponseEntity<?> manejarConcurrencia(ConcurrenciaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}