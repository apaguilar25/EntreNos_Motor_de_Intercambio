package app.model.CapaControladores;

import app.model.CapaEntidades.Imagen;
import app.model.CapaEntidades.Monedero;
import app.model.CapaEntidades.Usuario;
import app.model.CapaGestion.GestionUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
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

<<<<<<< HEAD

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        try {
            // 1. Criterio de Aceptación HU1: Validar dominio del correo comunitario
            if (nuevoUsuario.getCorreoElectronico() == null || !nuevoUsuario.getCorreoElectronico().endsWith("@alameda.com")) {
                return ResponseEntity.badRequest().body("El correo debe pertenecer al dominio oficial alameda.com");
            }

            // 2. Completar los datos automáticos requeridos por la HU1
            nuevoUsuario.setIdUsuario(UUID.randomUUID().toString()); // ID único automático
            nuevoUsuario.setMonedero(new Monedero());       // Capital Semilla inicial obligado

            // 3. Manejo del atributo foto (Si viene vacío desde el frente, le asignamos un texto por defecto)
            if (nuevoUsuario.getFotoPerfil() == null)
                nuevoUsuario.setFotoPerfil(new Imagen("avatarPersona")); // 💡 Cambia 'setRutaFoto' por el nombre exacto de tu atributo string
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar el usuario: " + e.getMessage());
        }

        // 4. Tu función de lectura y sobreescritura física en el archivo usuarios.json
        gestionUsuario.registrarUsuario(nuevoUsuario);

        // Respondemos al Frontend con el usuario ya guardado
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);

    }

=======
    // DTO interno para recibir el catálogo
    static class CatalogoDTO {
        public java.util.List<app.model.CapaEntidades.Habilidad> habilidades;
        public java.util.List<app.model.CapaEntidades.Necesidad> necesidades;
    }

    // Actualizar el catálogo (habilidades y necesidades)
    @PutMapping("/{id}/catalogo")
    public ResponseEntity<String> actualizarCatalogo(@PathVariable String id, @RequestBody CatalogoDTO catalogoDTO) {
        try {
            gestionUsuario.actualizarCatalogo(id, catalogoDTO.habilidades, catalogoDTO.necesidades);
            return ResponseEntity.ok("{\"mensaje\": \"Catálogo actualizado correctamente.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
>>>>>>> db4991d (Conexion Back-Front HU1 y HU2 completo (creo))
}