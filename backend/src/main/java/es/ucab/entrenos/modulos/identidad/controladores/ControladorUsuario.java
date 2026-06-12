package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.dtos.*;
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

    public ControladorUsuario(ServicioUsuario servicioUsuario, ServicioHabilidad servicioHabilidad) {
        this.servicioUsuario = servicioUsuario;
        this.servicioHabilidad = servicioHabilidad;
    }

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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/catalogo")
    public ResponseEntity<?> configurarCatalogo(@PathVariable String id, @RequestBody ConfiguracionCatalogoRequestDTO request) {
        try {
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
            @RequestBody EdicionOfertaDTO request) {
        try {
            // Ya no buscamos la categoría, delegamos la edición directamente usando el idInstancia
            servicioUsuario.editarHabilidadOfrecida(
                    id,
                    request.getIdInstancia(),
                    request.getPrecioCreditos(),
                    request.getDescripcionServicio()
            );

            return ResponseEntity.ok().body("Oferta específica editada con éxito.");
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
            @RequestBody EdicionNecesidadDTO request) {
        try {
            // Al igual que las ofertas, delegamos la edición usando el idInstancia
            servicioUsuario.editarNecesidadRegistrada(
                    id,
                    request.getIdInstancia(),
                    request.getDescripcionCondiciones()
            );

            return ResponseEntity.ok().body("Necesidad específica editada con éxito.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Eliminar habilidad ofrecida
    @DeleteMapping("/{id}/habilidades/{idInstancia}")
    public ResponseEntity<?> eliminarHabilidadOfrecida(
            @PathVariable String id,
            @PathVariable String idInstancia) {
        try {
            servicioUsuario.eliminarHabilidadOfrecida(id, idInstancia);
            return ResponseEntity.ok().body("Oferta eliminada del catálogo con éxito.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Eliminar necesidad registrada
    @DeleteMapping("/{id}/necesidades/{idInstancia}")
    public ResponseEntity<?> eliminarNecesidadRegistrada(
            @PathVariable String id,
            @PathVariable String idInstancia) {
        try {
            servicioUsuario.eliminarNecesidadRegistrada(id, idInstancia);
            return ResponseEntity.ok().body("Necesidad eliminada del catálogo con éxito.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


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

    @GetMapping("/{id}/saldo")
    public ResponseEntity<?> obtenerSaldo(@PathVariable String id) {
        Optional<Usuario> usuarioOpt = servicioUsuario.buscarPorId(id);
        if (usuarioOpt.isPresent()) {
            Monedero m = usuarioOpt.get().getMonedero();
            SaldoResponseDTO saldo = new SaldoResponseDTO(m.getCreditosDisponibles(), m.getCreditosRetenidos());
            return ResponseEntity.ok(saldo);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPerfil(@PathVariable String id) {
        java.util.Optional<Usuario> usuarioOpt = servicioUsuario.buscarPorId(id);

        if (usuarioOpt.isPresent()) {
            PerfilUsuarioResponseDTO perfilSaneado = new PerfilUsuarioResponseDTO(usuarioOpt.get());
            return ResponseEntity.ok(perfilSaneado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
    }

    // Agregar Habilidad a Usuario
    @PostMapping("/{id}/habilidades")
    public ResponseEntity<?> agregarHabilidadIndividual(
            @PathVariable String id,
            @RequestBody NuevaOfertaIndividualDTO request) {
        try {
            // 1. Validamos que la categoría maestra exista
            Habilidad habilidadBase = servicioHabilidad.buscarPorId(request.getIdHabilidadCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("La categoría de habilidad maestra no existe en el sistema."));

            // 2. Agregamos al usuario
            servicioUsuario.agregarHabilidadIndividual(id, habilidadBase, request.getPrecioCreditos(), request.getDescripcionServicio());

            return ResponseEntity.status(HttpStatus.CREATED).body("Habilidad agregada al catálogo del usuario con éxito.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Agregar Necesidad a Usuario
    @PostMapping("/{id}/necesidades")
    public ResponseEntity<?> agregarNecesidadIndividual(
            @PathVariable String id,
            @RequestBody NuevaNecesidadIndividualDTO request) {
        try {
            // 1. Validamos que la categoría maestra exista
            Habilidad necesidadBase = servicioHabilidad.buscarPorId(request.getIdHabilidadCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("La categoría de habilidad maestra no existe en el sistema."));

            // 2. Agregamos al usuario
            servicioUsuario.agregarNecesidadIndividual(id, necesidadBase, request.getDescripcionCondiciones());

            return ResponseEntity.status(HttpStatus.CREATED).body("Necesidad agregada al catálogo del usuario con éxito.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
