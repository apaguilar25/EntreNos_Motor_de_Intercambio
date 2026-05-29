package es.ucab.entrenos.nucleo.seguridad.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

// En la capa de Seguridad / Servicios (Lógica de control reutilizable)
@Service
public class ServicioAutenticacion{

    private final IRepositorioUsuario repositorioUsuario;
    private final BCryptPasswordEncoder encriptador; // Herramienta de Spring Boot

    // Inyección de dependencias por constructor (Mejor práctica en Spring Boot)
    public ServicioAutenticacion(IRepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
        this.encriptador = new BCryptPasswordEncoder();
    }

    private boolean contrasenaCorrecta(Usuario usuario, String contrasenaPlana) {
        // passwordEncoder.matches() verifica que la contraseña ingresada sea correcta
        return encriptador.matches(contrasenaPlana, usuario.getContrasenaHash());
    }

    public boolean login(String correo, String contrasena) {
        // Usuario que puede, o no, existir
        Optional<Usuario> usuarioOpt = repositorioUsuario.buscarPorCorreo(correo);

        // Valida si existe o no
        if (usuarioOpt.isEmpty()) {
            throw new SecurityException("Credenciales inválidas.");
        }

        // Retorna el usuario existente
        Usuario usuario = usuarioOpt.get();

        if (usuario.isCuentaBloqueada()) {
            // Valida si ya pasaron las 24 horas para desbloquear
            if (System.currentTimeMillis() > usuario.getTiempoDesbloqueoMillis()) {
                usuario.desbloquearCuenta(); // Se limpia el estado
                repositorioUsuario.guardar(usuario);
            } else {
                throw new SecurityException("La cuenta se encuentra bloqueada.");
            }
        }

        if (contrasenaCorrecta(usuario, contrasena)) {
            usuario.resetearIntentosFallidos();
            repositorioUsuario.guardar(usuario);
            return true;
        } else {
            // --- LÓGICA ESTRICTA ERS: VENTANA DE 3 MINUTOS ---
            long ahora = System.currentTimeMillis();
            long ventanaTresMinutos = 3L * 60 * 1000;

            // 1. Verificamos si la ventana de 3 minutos ya caducó
            // Si ya pasaron más de 3 minutos desde el primer error, la cuenta de errores se limpia
            if (usuario.getIntentosFallidos() > 0 && (ahora - usuario.getPrimerIntentoFallidoMillis() > ventanaTresMinutos)) {
                usuario.resetearIntentosFallidos();
            }

            // 2. Registramos el error actual
            if (usuario.getIntentosFallidos() == 0) {
                // Es el primer error de la ráfaga, iniciamos el cronómetro
                usuario.setPrimerIntentoFallidoMillis(ahora);
            }
            usuario.incrementarIntentosFallidos();

            // 3. Verificamos si en esta ventana llegó al límite de 5
            if (usuario.getIntentosFallidos() >= 5) {
                usuario.bloquearCuenta(24L * 60 * 60 * 1000); // 24 horas bloqueado
            }

            repositorioUsuario.guardar(usuario);

            throw new SecurityException("Credenciales inválidas.");
        }
    }
}