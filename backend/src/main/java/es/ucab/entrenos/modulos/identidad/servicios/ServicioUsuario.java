package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.excepciones.CorreoDuplicadoException;
import es.ucab.entrenos.modulos.identidad.excepciones.TelefonoDuplicadoException;
import es.ucab.entrenos.modulos.identidad.modelos.HabilidadOfrecida;
import es.ucab.entrenos.modulos.identidad.modelos.NecesidadRegistrada;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServicioUsuario {

    private final IRepositorioUsuario repositorioUsuario;
    private final BCryptPasswordEncoder encriptador;

    // El capital semilla definido en el ERS
    private static final float CAPITAL_SEMILLA_INICIAL = 50.0f;

    public ServicioUsuario(IRepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
        this.encriptador = new BCryptPasswordEncoder();
    }

    public Optional<Usuario> buscarPorId(String id) {
        return repositorioUsuario.buscarPorId(id);
    }

    //  Registro inicial del usuario (Aún sin catálogo)
    public Usuario registrarUsuario(String nombre, String correoElectronico, String telefono, String descripcionPersonal, String contrasenaPlana) {

        // 1. Validar que el correo no esté registrado previamente
        Optional<Usuario> usuarioMismoCorreo = repositorioUsuario.buscarPorCorreo(correoElectronico);
        if (usuarioMismoCorreo.isPresent()) {
            throw new CorreoDuplicadoException("El correo electrónico ya se encuentra registrado en la comunidad.");
        }

        // 2. NUEVO: Validar que el teléfono no esté registrado previamente
        Optional<Usuario> usuarioMismoTelefono = repositorioUsuario.buscarPorTelefono(telefono);
        if (usuarioMismoTelefono.isPresent()) {
            throw new TelefonoDuplicadoException("El número de teléfono ya está asociado a otra cuenta en la comunidad.");
        }

        // 3. Encriptar la contraseña
        String contrasenaHash = encriptador.encode(contrasenaPlana);

        // 4. Generar un ID único universal para el usuario
        String nuevoId = UUID.randomUUID().toString();

        // 5. Crear el objeto Usuario (nace con Monedero en 0 y catalogoCompletado en false)
        Usuario nuevoUsuario = new Usuario(
                nuevoId,
                nombre,
                correoElectronico,
                telefono,
                descripcionPersonal,
                contrasenaHash
        );

        // 6. Guardar en el JSON a través del repositorio
        repositorioUsuario.guardar(nuevoUsuario);

        return nuevoUsuario;
    }


    // Completar el catálogo y recibir el Capital Semilla

    public void configurarCatalogo(String idUsuario, List<HabilidadOfrecida> ofertas, List<NecesidadRegistrada> necesidades) {

        // 1. Buscar al usuario en la base de datos (JSON)
        Optional<Usuario> usuarioOpt = buscarPorId(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOpt.get();

        // 2. Validar que no haya reclamado el capital antes
        if (usuario.isCatalogoCompletado()) {
            throw new IllegalStateException("El catálogo ya fue completado anteriormente.");
        }

        // 3. Agregar todas las ofertas (valida internamente que no sean nulas ni repetidas)
        for (HabilidadOfrecida oferta : ofertas) {
            usuario.agregarHabilidadOfrecida(oferta);
        }

        // 4. Agregar todas las necesidades
        for (NecesidadRegistrada necesidad : necesidades) {
            usuario.agregarNecesidad(necesidad);
        }

        // 5. Regla de Negocio: Inyectar el capital semilla automáticamente al terminar
        usuario.finalizarConfiguracionCatalogo(CAPITAL_SEMILLA_INICIAL);

        // 6. Sobreescribir el usuario en el JSON con su nuevo saldo y habilidades
        repositorioUsuario.guardar(usuario);
    }

    // Carga/Actualización de la Foto de Perfil (Criterio de Aceptación 1)
    public void actualizarFotoPerfil(String idUsuario, String urlNuevaFoto) {
        Optional<Usuario> usuarioOpt = buscarPorId(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOpt.get();
        if (urlNuevaFoto == null || urlNuevaFoto.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de la foto no puede estar vacía.");
        }

        usuario.setUrlFotoPerfil(urlNuevaFoto);
        repositorioUsuario.guardar(usuario);
    }


    //  PASO 4: Edición de Habilidades Ofrecidas (Criterios de Aceptación 5 y 6)
    //  Permite editar el precio en créditos y la descripción en cualquier momento.
    public void editarHabilidadOfrecida(String idUsuario, HabilidadOfrecida habilidadEditada) {
        Optional<Usuario> usuarioOpt = buscarPorId(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOpt.get();

        // Buscamos si el usuario realmente tiene esta habilidad registrada
        // (El equals() que programaste nos ayuda a encontrarla por la Habilidad Base)
        usuario.removerHabilidadOfrecida(habilidadEditada); // Quitamos la versión vieja
        usuario.agregarHabilidadOfrecida(habilidadEditada); // Agregamos la nueva versión con precio/descripción actualizados

        repositorioUsuario.guardar(usuario);
    }

    /**
     * PASO 5: Edición de Necesidades Registradas (Criterio de Aceptación 6)
     * Permite editar la descripción (condiciones) en cualquier momento.
     */
    public void editarNecesidadRegistrada(String idUsuario, NecesidadRegistrada necesidadEditada) {
        Optional<Usuario> usuarioOpt = buscarPorId(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOpt.get();

        usuario.removerNecesidad(necesidadEditada); // Quitamos la vieja
        usuario.agregarNecesidad(necesidadEditada); // Agregamos la actualizada

        repositorioUsuario.guardar(usuario);
    }

}