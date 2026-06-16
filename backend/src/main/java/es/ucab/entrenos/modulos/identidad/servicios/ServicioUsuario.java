package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.excepciones.CorreoDuplicadoException;
import es.ucab.entrenos.modulos.identidad.excepciones.TelefonoDuplicadoException;
import es.ucab.entrenos.modulos.identidad.modelos.EstadoCuenta;
import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
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

    public void guardar(Usuario usuario) {
        repositorioUsuario.guardar(usuario);
    }

    public void actualizarReputacion(String idReceptor, float calificacion) {
        // TODO implementar funcion
        return;
    }

    //  Registro inicial del usuario (Aún sin catálogo)
    public Usuario registrarUsuario(String nombre, String correoElectronico, String telefono,
                                    String descripcionPersonal, String contrasenaPlana) {

        // 1. VALIDACIÓN: Restricción de Dominio estricta
        if (correoElectronico == null || correoElectronico.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }

        // 2. Expresión regular que obliga a que el correo termine exactamente en @alameda.com
        String regexDominio = "^[A-Za-z0-9._%+-]+@alameda\\.com$";

        if (!correoElectronico.matches(regexDominio)) {
            throw new IllegalArgumentException("Error: El correo electrónico debe pertenecer exclusivamente " +
                    "al dominio oficial de la comunidad (@alameda.com).");
        }

        // 3. Validar que el correo no esté registrado previamente
        Optional<Usuario> usuarioMismoCorreo = repositorioUsuario.buscarPorCorreo(correoElectronico);
        if (usuarioMismoCorreo.isPresent()) {
            throw new CorreoDuplicadoException("El correo electrónico ya se encuentra registrado en la comunidad.");
        }

        // 4. Validar que el teléfono no esté registrado previamente
        Optional<Usuario> usuarioMismoTelefono = repositorioUsuario.buscarPorTelefono(telefono);
        if (usuarioMismoTelefono.isPresent()) {
            throw new TelefonoDuplicadoException("El número de teléfono ya está asociado a otra cuenta en la comunidad.");
        }

        // 5. Encriptar la contraseña
        String contrasenaHash = encriptador.encode(contrasenaPlana);

        // 6. Generar un ID único universal para el usuario
        String nuevoId = UUID.randomUUID().toString();

        // 7. Crear el objeto Usuario (nace con Monedero en 0, Estado ACTIVO y catalogoCompletado en false gracias al constructor)
        Usuario nuevoUsuario = new Usuario(
                nuevoId,
                nombre,
                correoElectronico,
                telefono,
                descripcionPersonal,
                contrasenaHash
        );

        // 8. Guardar en el JSON a través del repositorio
        guardar(nuevoUsuario);

        return nuevoUsuario;
    }


    // Completar el catálogo y recibir el Capital Semilla
    public void configurarCatalogo(String idUsuario, List<HabilidadOfrecida> ofertas, List<NecesidadRegistrada> necesidades) {
        Usuario usuario = buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        verificarUsuarioActivo(usuario); // Defensa en profundidad

        if (usuario.isCatalogoCompletado()) {
            throw new IllegalStateException("El catálogo ya fue completado anteriormente.");
        }

        for (HabilidadOfrecida oferta : ofertas) {
            usuario.agregarHabilidadOfrecida(oferta);
        }

        for (NecesidadRegistrada necesidad : necesidades) {
            usuario.agregarNecesidad(necesidad);
        }

        usuario.finalizarConfiguracionCatalogo(CAPITAL_SEMILLA_INICIAL);
        guardar(usuario);
    }

    // --- AGREGAR OFERTA INDIVIDUAL ---
    public HabilidadOfrecida agregarHabilidadIndividual(String idUsuario, Habilidad habilidadBase, int precioCreditos, String descripcionServicio) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        verificarUsuarioActivo(usuario); // Defensa en profundidad

        HabilidadOfrecida nuevaOferta = new HabilidadOfrecida(habilidadBase, precioCreditos, descripcionServicio);
        usuario.agregarHabilidadOfrecida(nuevaOferta);
        guardar(usuario);
        return nuevaOferta;
    }

    // --- AGREGAR NECESIDAD INDIVIDUAL ---
    public NecesidadRegistrada agregarNecesidadIndividual(String idUsuario, Habilidad necesidadBase, String descripcionCondiciones) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        verificarUsuarioActivo(usuario); // Defensa en profundidad

        NecesidadRegistrada nuevaNecesidad = new NecesidadRegistrada(necesidadBase, descripcionCondiciones);
        usuario.agregarNecesidad(nuevaNecesidad);
        guardar(usuario);
        return nuevaNecesidad;
    }

    // Carga/Actualización de la Foto de Perfil
    public void actualizarFotoPerfil(String idUsuario, String urlNuevaFoto) {
        Usuario usuario = buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        verificarUsuarioActivo(usuario); // Defensa en profundidad

        if (urlNuevaFoto == null || urlNuevaFoto.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de la foto no puede estar vacía.");
        }

        usuario.setUrlFotoPerfil(urlNuevaFoto);
        guardar(usuario);
    }


    // PASO 4: Edición de Habilidades Ofrecidas
    public void editarHabilidadOfrecida(String idUsuario, String idInstancia, int precioCreditos, String descripcionServicio) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        verificarUsuarioActivo(usuario); // Defensa en profundidad

        usuario.actualizarHabilidadOfrecida(idInstancia, precioCreditos, descripcionServicio);
        guardar(usuario);
    }

    // PASO 5: Edición de Necesidades Registradas
    public void editarNecesidadRegistrada(String idUsuario, String idInstancia, String descripcionCondiciones) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        verificarUsuarioActivo(usuario); // Defensa en profundidad

        usuario.actualizarNecesidadRegistrada(idInstancia, descripcionCondiciones);
        guardar(usuario);
    }

    public void eliminarHabilidadOfrecida(String idUsuario, String idInstancia) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        verificarUsuarioActivo(usuario); // Defensa en profundidad

        usuario.eliminarHabilidadOfrecida(idInstancia);
        guardar(usuario);
    }

    public void eliminarNecesidadRegistrada(String idUsuario, String idInstancia) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        verificarUsuarioActivo(usuario); // Defensa en profundidad

        usuario.eliminarNecesidadRegistrada(idInstancia);
        guardar(usuario);
    }

    // ========================================================================
    // UTILIDAD DE SEGURIDAD INTERNA (Defensa en Profundidad)
    // ========================================================================
    private void verificarUsuarioActivo(Usuario usuario) {
        // Aprovechamos la inteligencia del enum y del método getEstado()
        // para garantizar que ni usuarios bloqueados ni baneados hagan mutaciones.
        if (usuario.getEstado() != EstadoCuenta.ACTIVO) {
            throw new IllegalStateException("Operación denegada: La cuenta del usuario no se encuentra activa debido a una sanción o bloqueo de seguridad.");
        }
    }
}