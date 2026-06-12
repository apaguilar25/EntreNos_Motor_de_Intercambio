package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioHabilidad;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habilidades")
public class ControladorHabilidad {

    private final ServicioHabilidad servicioHabilidad;
    private final ServicioUsuario servicioUsuario; // Lo necesitamos para verificar el rol

    public ControladorHabilidad(ServicioHabilidad servicioHabilidad, ServicioUsuario servicioUsuario) {
        this.servicioHabilidad = servicioHabilidad;
        this.servicioUsuario = servicioUsuario;
    }

    // ACCESO PÚBLICO: Cualquier miembro de la comunidad puede ver el catálogo
    @GetMapping
    public ResponseEntity<List<Habilidad>> listarHabilidades() {
        return ResponseEntity.ok(servicioHabilidad.obtenerTodas());
    }

    // ACCESO RESTRINGIDO (SOLO ADMIN): Agregar al catálogo
    @PostMapping
    public ResponseEntity<?> crearHabilidad(
            @RequestHeader("X-Usuario-Id") String idSolicitante,
            @RequestBody java.util.Map<String, String> request) {
        try {
            verificarPermisosAdministrador(idSolicitante);

            String categoria = request.get("categoria");
            Habilidad nueva = servicioHabilidad.crearHabilidad(categoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ACCESO RESTRINGIDO (SOLO ADMIN): Editar el catálogo
    @PutMapping("/{id}")
    public ResponseEntity<?> editarHabilidad(
            @PathVariable String id,
            @RequestHeader("X-Usuario-Id") String idSolicitante,
            @RequestBody java.util.Map<String, String> request) {
        try {
            verificarPermisosAdministrador(idSolicitante);

            String nuevaCategoria = request.get("categoria");
            servicioHabilidad.editarHabilidad(id, nuevaCategoria);
            return ResponseEntity.ok().body("Habilidad maestra actualizada con éxito.");

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // --- UTILIDAD INTERNA DE SEGURIDAD ---
    private void verificarPermisosAdministrador(String idUsuario) {
        Usuario usuario = servicioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new SecurityException("Usuario no autenticado o no encontrado."));

        if (!usuario.isAdministrador()) {
            throw new SecurityException("Acceso denegado: Solo los administradores pueden modificar el catálogo global de habilidades.");
        }
    }
}