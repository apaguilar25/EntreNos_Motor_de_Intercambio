package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServicioAutenticacion {

    private final IRepositorioUsuario repositorioUsuario;
    private final BCryptPasswordEncoder encriptador;

    public ServicioAutenticacion(IRepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
        this.encriptador = new BCryptPasswordEncoder();
    }

    /**
     * Autentica al usuario evaluando primero en qué estado se encuentra su máquina de estados.
     */
    public Usuario login(String correo, String contrasenaPlana) {
        if (correo == null || correo.trim().isEmpty() || contrasenaPlana == null || contrasenaPlana.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo y la contraseña son campos requeridos.");
        }

        String correoLimpio = correo.trim().toLowerCase();

        // 1. VERIFICACIÓN DE EXISTENCIA
        Optional<Usuario> usuarioOpt = repositorioUsuario.buscarPorCorreo(correoLimpio);
        if (usuarioOpt.isEmpty()) {
            throw new SecurityException("Credenciales inválidas.");
        }

        Usuario usuario = usuarioOpt.get();

        // 2. EVALUACIÓN DE LA MÁQUINA DE ESTADOS
        // El mét.odo getEstado() de Usuario ya se encargó de auto-desbloquear si el tiempo caducó.
        switch (usuario.getEstado()) {

            case SUSPENDIDO_FRAUDE:
                throw new SecurityException("Acceso denegado: Esta cuenta ha sido inhabilitada permanentemente por la administración debido a violaciones éticas.");

            case SUSPENDIDO_SUBASTA:
                long horasSubasta = (usuario.getTiempoDesbloqueoMillis() - System.currentTimeMillis()) / (1000 * 60 * 60);
                throw new SecurityException("Cuenta suspendida temporalmente por inactividad o abandono en subastas. Intente nuevamente en " + (horasSubasta + 1) + " horas.");

            case BLOQUEADO_SEGURIDAD:
                long horasSeguridad = (usuario.getTiempoDesbloqueoMillis() - System.currentTimeMillis()) / (1000 * 60 * 60);
                throw new SecurityException("Tu cuenta ha sido inhabilitada temporalmente por 5 intentos en menos de 3 minutos. Intente nuevamente en " + (horasSeguridad + 1) + " horas.");

            case ACTIVO:
                // 3. VALIDACIÓN CRIPTOGRÁFICA DE LA CONTRASEÑA
                if (encriptador.matches(contrasenaPlana, usuario.getContrasenaHash())) {
                    // ÉXITO
                    usuario.registrarInicioSesionExitoso();
                    usuario.incrementarVersion();
                    repositorioUsuario.guardar(usuario);

                    return usuario;
                } else {
                    // FALLO
                    usuario.registrarIntentoFallido();
                    repositorioUsuario.guardar(usuario);
                    throw new SecurityException("Credenciales inválidas.");
                }

            default:
                throw new IllegalStateException("Estado de cuenta desconocido.");
        }
    }
}