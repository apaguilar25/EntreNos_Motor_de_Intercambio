package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.gamificacion.dtos.LogroDesbloqueadoResponseDTO;
import es.ucab.entrenos.modulos.gamificacion.servicios.ServicioGamificacion;
import es.ucab.entrenos.modulos.gamificacion.servicios.ServicioPodio;
import es.ucab.entrenos.modulos.identidad.dtos.*;
import es.ucab.entrenos.modulos.identidad.excepciones.CorreoDuplicadoException;
import es.ucab.entrenos.modulos.identidad.excepciones.TelefonoDuplicadoException;
import es.ucab.entrenos.modulos.identidad.modelos.*;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioHabilidad;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.servicios.ServicioPublicacion;
import es.ucab.entrenos.modulos.gamificacion.dtos.LogroDesbloqueadoResponseDTO;
import es.ucab.entrenos.modulos.subasta.dtos.SubastaResumenDTO;
import es.ucab.entrenos.modulos.subasta.servicios.ServicioSubasta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import es.ucab.entrenos.modulos.publicacion.servicios.ServicioPublicacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class ControladorUsuario {

    private final ServicioUsuario servicioUsuario;
    private final ServicioHabilidad servicioHabilidad;
    private final ServicioPublicacion servicioPublicacion;

    private final ServicioSubasta servicioSubasta;
    private final ServicioGamificacion servicioGamificacion;
    private final ServicioPodio servicioPodio;

    public ControladorUsuario(
            ServicioUsuario servicioUsuario,
            ServicioHabilidad servicioHabilidad,
            ServicioPublicacion servicioPublicacion,
            ServicioSubasta servicioSubasta,
            ServicioGamificacion servicioGamificacion,
            ServicioPodio servicioPodio
    ) {
        this.servicioUsuario = servicioUsuario;
        this.servicioHabilidad = servicioHabilidad;
        this.servicioPublicacion = servicioPublicacion;
        this.servicioSubasta = servicioSubasta;
        this.servicioGamificacion = servicioGamificacion;
        this.servicioPodio = servicioPodio;
    }

    private void generarPublicacionDesdeCatalogo(Usuario usuario, String idInstancia, String tipo, Habilidad habilidadBase, String descripcion, int precio) {
        Publicacion pub = new Publicacion(
                usuario.getId(),
                tipo,
                habilidadBase.getCategoria(),
                descripcion,
                precio
        );
        pub.setIdInstanciaCatalogo(idInstancia);
        servicioPublicacion.crearPublicacion(pub);
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

            // --- Sincronización con el Muro ---
            Usuario usuarioOpt = servicioUsuario.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado tras configurar catálogo"));
            for (HabilidadOfrecida o : ofertas) {
                generarPublicacionDesdeCatalogo(usuarioOpt, o.getIdInstancia(), "HABILIDAD", o.getHabilidadBase(), o.getDescripcionServicio(), o.getPrecioCreditos());
            }
            for (NecesidadRegistrada n : necesidades) {
                generarPublicacionDesdeCatalogo(usuarioOpt, n.getIdInstancia(), "NECESIDAD", n.getNecesidadBase(), n.getDescripcionCondiciones(), 0);
            }

            return ResponseEntity.ok().body("Catálogo configurado y Capital Semilla asignado con éxito.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/habilidades")
    public ResponseEntity<?> editarHabilidadOfrecida(
            @PathVariable String id,
            @RequestBody EdicionOfertaDTO request) {
        try {
            servicioUsuario.editarHabilidadOfrecida(
                    id, request.getIdInstancia(),
                    request.getPrecioCreditos(), request.getDescripcionServicio());

            servicioPublicacion.obtenerPublicacionPorInstanciaCatalogo(request.getIdInstancia())
                    .ifPresent(pub -> {
                        pub.setDescripcion(request.getDescripcionServicio());
                        pub.setPrecioCreditos(request.getPrecioCreditos());
                        servicioPublicacion.guardarPublicacion(pub);
                    });

            try {
                servicioPublicacion.actualizarPublicacion(request.getIdInstancia(), request.getPrecioCreditos(), request.getDescripcionServicio());
            } catch (Exception e) {
                System.out.println("Aviso: No se encontró la publicación en el muro para actualizar: " + request.getIdInstancia());
            }

            return ResponseEntity.ok().body("Oferta específica editada con éxito.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/necesidades")
    public ResponseEntity<?> editarNecesidadRegistrada(
            @PathVariable String id,
            @RequestBody EdicionNecesidadDTO request) {
        try {
            servicioUsuario.editarNecesidadRegistrada(
                    id, request.getIdInstancia(), request.getDescripcionCondiciones());

            servicioPublicacion.obtenerPublicacionPorInstanciaCatalogo(request.getIdInstancia())
                    .ifPresent(pub -> {
                        pub.setDescripcion(request.getDescripcionCondiciones());
                        servicioPublicacion.guardarPublicacion(pub);
                    });

            try {
                servicioPublicacion.actualizarPublicacion(request.getIdInstancia(), 0, request.getDescripcionCondiciones());
            } catch (Exception e) {
                System.out.println("Aviso: No se encontró la publicación en el muro para actualizar: " + request.getIdInstancia());
            }

            return ResponseEntity.ok().body("Necesidad específica editada con éxito.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/habilidades/{idInstancia}")
    public ResponseEntity<?> eliminarHabilidadOfrecida(
            @PathVariable String id,
            @PathVariable String idInstancia) {
        try {
            servicioUsuario.eliminarHabilidadOfrecida(id, idInstancia);
            servicioPublicacion.eliminarPublicacion(idInstancia);
            return ResponseEntity.ok().body("Oferta eliminada del catálogo con éxito.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/necesidades/{idInstancia}")
    public ResponseEntity<?> eliminarNecesidadRegistrada(
            @PathVariable String id,
            @PathVariable String idInstancia) {
        try {
            servicioUsuario.eliminarNecesidadRegistrada(id, idInstancia);
            servicioPublicacion.eliminarPublicacion(idInstancia);
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
            SaldoResponseDTO saldo = new SaldoResponseDTO(m.getCreditosDisponibles(), m.getCreditosComprometidos());
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

    @GetMapping("/{id}/perfil-publico")
    public ResponseEntity<PerfilPublicoCompletoDTO> obtenerPerfilPublico(@PathVariable String id) {

        // 1. Obtenemos los datos base de identidad
        PerfilPublicoDTO perfilBasico = servicioUsuario.obtenerPerfilPublico(id);

        // 2. Obtenemos información transversal
        // NOTA: Ajusta los nombres de estos métodos ("obtenerSubastasPorUsuario", etc.)
        // para que coincidan exactamente con cómo los llamaste dentro de tus servicios.
        List<SubastaResumenDTO> misSubastas = servicioSubasta.obtenerSubastasPorUsuario(id);
        List<LogroDesbloqueadoResponseDTO> misLogros = servicioGamificacion.obtenerLogrosPorUsuario(id);
        boolean podio = servicioPodio.estaEnPodioSemanal(id);

        // 3. Ensamblamos la respuesta completa
        PerfilPublicoCompletoDTO perfilCompleto = new PerfilPublicoCompletoDTO(
                perfilBasico,
                misSubastas,
                misLogros,
                podio
        );

        return ResponseEntity.ok(perfilCompleto);
    }
    // Agregar Habilidad a Usuario
    @PostMapping("/{id}/habilidades")
    public ResponseEntity<?> agregarHabilidadIndividual(
            @PathVariable String id,
            @RequestBody NuevaOfertaIndividualDTO request) {
        try {
            Habilidad habilidadBase = servicioHabilidad.buscarPorId(request.getIdHabilidadCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("La categoría de habilidad maestra no existe en el sistema."));

            // 2. Agregamos al usuario
            HabilidadOfrecida nueva = servicioUsuario.agregarHabilidadIndividual(id, habilidadBase, request.getPrecioCreditos(), request.getDescripcionServicio());

            // 3. Sincronizamos con el muro
            Usuario usuarioOpt = servicioUsuario.buscarPorId(id).orElseThrow();
            generarPublicacionDesdeCatalogo(usuarioOpt, nueva.getIdInstancia(), "HABILIDAD", habilidadBase, request.getDescripcionServicio(), request.getPrecioCreditos());

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
            Habilidad necesidadBase = servicioHabilidad.buscarPorId(request.getIdHabilidadCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("La categoría de habilidad maestra no existe en el sistema."));

            // 2. Agregamos al usuario
            NecesidadRegistrada nueva = servicioUsuario.agregarNecesidadIndividual(id, necesidadBase, request.getDescripcionCondiciones());

            // 3. Sincronizamos con el muro
            Usuario usuarioOpt = servicioUsuario.buscarPorId(id).orElseThrow();
            generarPublicacionDesdeCatalogo(usuarioOpt, nueva.getIdInstancia(), "NECESIDAD", necesidadBase, request.getDescripcionCondiciones(), 0);

            return ResponseEntity.status(HttpStatus.CREATED).body("Necesidad agregada al catálogo del usuario con éxito.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
