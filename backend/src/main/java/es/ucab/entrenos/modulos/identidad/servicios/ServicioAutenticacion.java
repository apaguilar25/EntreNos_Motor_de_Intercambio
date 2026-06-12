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

    // Parámetros estrictos definidos en la especificación ERS del sistema
    private static final long VENTANA_TRES_MINUTOS_MS = 3L * 60 * 1000;
    private static final long BLOQUEO_VEINTICUATRO_HORAS_MS = 24L * 60 * 60 * 1000;
    private static final int MAX_INTENTOS_FALLIDOS = 5;

    public ServicioAutenticacion(IRepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
        this.encriptador = new BCryptPasswordEncoder();
    }

    private boolean contrasenaCorrecta(Usuario usuario, String contrasenaPlana) {
        return encriptador.matches(contrasenaPlana, usuario.getContrasenaHash());
    }

    public boolean login(String correo, String contrasena) {
        String correoLimpio = correo.trim().toLowerCase();
        long ahora = System.currentTimeMillis();

        // 1. VERIFICACIÓN DE EXISTENCIA
        Optional<Usuario> usuarioOpt = repositorioUsuario.buscarPorCorreo(correoLimpio);
        if (usuarioOpt.isEmpty()) {
            throw new SecurityException("Credenciales inválidas.");
        }
        Usuario usuario = usuarioOpt.get();

        // 2. INTEGRIDAD ÉTICA (Bloqueo Permanente por Fraude - Requisito ERS)
        if (usuario.isBaneadoPermanentemente()) {
            throw new SecurityException("Acceso denegado: Esta cuenta ha sido inhabilitada permanentemente por la administración debido a reportes de fraude validados.");
        }

        // 3. CONTROL DE FUERZA BRUTA (Bloqueo Temporal de 24h - Requisito ERS)
        if (usuario.evaluarBloqueoTemporal(ahora)) {
            repositorioUsuario.guardar(usuario);
            throw new SecurityException("La cuenta se encuentra bloqueada temporalmente por seguridad.");
        }

        // 4. VALIDACIÓN DE CREDENCIALES
        if (contrasenaCorrecta(usuario, contrasena)) {
            usuario.registrarInicioSesionExitoso();
            repositorioUsuario.guardar(usuario);
            return true;
        } else {
            // El usuario falló: delegamos el cálculo de la ventana de 3 minutos y 5 intentos a la entidad
            usuario.registrarIntentoFallido(VENTANA_TRES_MINUTOS_MS, MAX_INTENTOS_FALLIDOS, BLOQUEO_VEINTICUATRO_HORAS_MS);
            repositorioUsuario.guardar(usuario);
            throw new SecurityException("Credenciales inválidas.");
        }
    }


}