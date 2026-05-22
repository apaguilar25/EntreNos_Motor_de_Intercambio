package app.model.CapaControlador;

import app.model.CapaEntidades.Usuario;
import app.model.CapaGestion.GestionUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioController {

    @Autowired
    private GestionUsuario gestionUsuario;

    // Obtener los datos de un usuario específico para pintar su perfil en React
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPerfil(@PathVariable String id) {
        try {
            Usuario usuario = gestionUsuario.buscarUsuario(id);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Modificar datos del perfil desde un formulario en React
    @PutMapping("/{id}/editar")
    public ResponseEntity<String> editarPerfil(
            @PathVariable String id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String descripcion) {
        try {
            gestionUsuario.modificarDatosUsuario(id, nombre, telefono, descripcion);
            return ResponseEntity.ok("{\"mensaje\": \"Perfil actualizado correctamente.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}