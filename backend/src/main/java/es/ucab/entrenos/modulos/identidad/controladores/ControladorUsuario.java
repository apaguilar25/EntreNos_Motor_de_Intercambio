package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.dtos.ConfiguracionCatalogoRequestDTO;
import es.ucab.entrenos.modulos.identidad.dtos.PerfilUsuarioResponseDTO;
import es.ucab.entrenos.modulos.identidad.dtos.RegistroUsuarioRequestDTO;
import es.ucab.entrenos.modulos.identidad.excepciones.CorreoDuplicadoException;
import es.ucab.entrenos.modulos.identidad.excepciones.TelefonoDuplicadoException;
import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.modelos.HabilidadOfrecida;
import es.ucab.entrenos.modulos.identidad.modelos.NecesidadRegistrada;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioHabilidad;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class ControladorUsuario {

    private final ServicioUsuario servicioUsuario;
    private final ServicioHabilidad servicioHabilidad;

    // Inyección de dependencias por constructor
    public ControladorUsuario(ServicioUsuario servicioUsuario, ServicioHabilidad servicioHabilidad) {
        this.servicioUsuario = servicioUsuario;
        this.servicioHabilidad = servicioHabilidad;
    }

    /**
     * Endpoint 1: Registro inicial del usuario
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroUsuarioRequestDTO request) {
        try {
            Usuario nuevoUsuario = servicioUsuario.registrarUsuario(
                    request.getNombre(),
                    request.getCorreoElectronico(),
                    request.getTelefono(),
                    request.getDescripcionPersonal(),
                    request.getContrasena()
            );

            PerfilUsuarioResponseDTO response = new PerfilUsuarioResponseDTO(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (CorreoDuplicadoException | TelefonoDuplicadoException e) {
            // Atrapamos los errores de duplicidad de datos
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict es ideal para duplicados

        } catch (IllegalArgumentException e) {
            // Atrapamos errores generales (ej. campos vacíos)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint 2: Configuración del Catálogo y Capital Semilla
     */
    @PostMapping("/{id}/catalogo")
    public ResponseEntity<?> configurarCatalogo(@PathVariable String id, @RequestBody ConfiguracionCatalogoRequestDTO request) {
        try {
            // Se eliminó el bloque duplicado. Ahora solo usamos el bloque validado.
            List<HabilidadOfrecida> ofertas = new ArrayList<>();
            if (request.getOfertas() != null) {
                for (ConfiguracionCatalogoRequestDTO.OfertaDTO o : request.getOfertas()) {
                    Habilidad baseReal = servicioHabilidad.buscarPorId(o.getIdHabilidadCategoria())
                            .orElseThrow(() -> new IllegalArgumentException("La habilidad " + o.getIdHabilidadCategoria() + " no es válida."));

                    ofertas.add(new HabilidadOfrecida(baseReal, o.getPrecioCreditos(), o.getDescripcionServicio()));
                }
            }

            List<NecesidadRegistrada> necesidades = new ArrayList<>();
            if (request.getNecesidades() != null) {
                for (ConfiguracionCatalogoRequestDTO.NecesidadDTO n : request.getNecesidades()) {
                    Habilidad baseReal = servicioHabilidad.buscarPorId(n.getIdHabilidadCategoria())
                            .orElseThrow(() -> new IllegalArgumentException("La necesidad " + n.getIdHabilidadCategoria() + " no es válida."));

                    necesidades.add(new NecesidadRegistrada(baseReal, n.getDescripcionCondiciones()));
                }
            }

            servicioUsuario.configurarCatalogo(id, ofertas, necesidades);
            return ResponseEntity.ok().body("Catálogo configurado y Capital Semilla asignado con éxito.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint 3: Editar una habilidad ofrecida existente
     */
    @PutMapping("/{id}/habilidades")
    public ResponseEntity<?> editarHabilidadOfrecida(
            @PathVariable String id,
            @RequestBody ConfiguracionCatalogoRequestDTO.OfertaDTO request) {
        try {
            // MEJORA: También validamos que la categoría exista al momento de editar
            Habilidad baseReal = servicioHabilidad.buscarPorId(request.getIdHabilidadCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("La habilidad " + request.getIdHabilidadCategoria() + " no es válida."));

            HabilidadOfrecida habilidadEditada = new HabilidadOfrecida(baseReal, request.getPrecioCreditos(), request.getDescripcionServicio());

            servicioUsuario.editarHabilidadOfrecida(id, habilidadEditada);
            return ResponseEntity.ok().body("Habilidad editada con éxito.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint 4: Editar una necesidad registrada existente
     */
    @PutMapping("/{id}/necesidades")
    public ResponseEntity<?> editarNecesidadRegistrada(
            @PathVariable String id,
            @RequestBody ConfiguracionCatalogoRequestDTO.NecesidadDTO request) {
        try {
            // MEJORA: También validamos que la categoría exista al momento de editar
            Habilidad baseReal = servicioHabilidad.buscarPorId(request.getIdHabilidadCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("La necesidad " + request.getIdHabilidadCategoria() + " no es válida."));

            NecesidadRegistrada necesidadEditada = new NecesidadRegistrada(baseReal, request.getDescripcionCondiciones());

            servicioUsuario.editarNecesidadRegistrada(id, necesidadEditada);
            return ResponseEntity.ok().body("Necesidad editada con éxito.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint 5: Actualizar foto de perfil
     */
    @PatchMapping("/{id}/foto")
    public ResponseEntity<?> actualizarFotoPerfil(
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String nuevaUrl = request.get("urlFoto");
            servicioUsuario.actualizarFotoPerfil(id, nuevaUrl);
            return ResponseEntity.ok().body("Foto de perfil actualizada con éxito.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint 6: Obtener el perfil completo del usuario
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPerfil(@PathVariable String id) {
        // Asume que agregaste buscarPorId en ServicioUsuario
        java.util.Optional<Usuario> usuarioOpt = servicioUsuario.buscarPorId(id);

        if (usuarioOpt.isPresent()) {
            PerfilUsuarioResponseDTO perfilSaneado = new PerfilUsuarioResponseDTO(usuarioOpt.get());
            return ResponseEntity.ok(perfilSaneado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
    }
}