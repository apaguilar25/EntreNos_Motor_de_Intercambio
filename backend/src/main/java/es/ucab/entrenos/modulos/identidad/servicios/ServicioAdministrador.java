package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import org.springframework.stereotype.Service;

@Service
public class ServicioAdministrador {

    private final IRepositorioUsuario repositorioUsuario;
    private final ServicioHabilidad servicioHabilidad;
    private final ServicioAutenticacion servicioAutenticacion;

    public ServicioAdministrador(IRepositorioUsuario repositorioUsuario, ServicioHabilidad servicioHabilidad, ServicioAutenticacion servicioAutenticacion) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicioHabilidad = servicioHabilidad;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    /**
     * Requisito ERS: Validar reporte de fraude y aplicar sanción.
     */
    public void validarReporteDeFraude(String idAdministrador, String idUsuarioInfractor) {
        verificarPermisosAdmin(idAdministrador);

        Usuario infractor = repositorioUsuario.buscarPorId(idUsuarioInfractor)
                .orElseThrow(() -> new IllegalArgumentException("Usuario infractor no encontrado."));

        // Llama al método de negocio que creamos anteriormente en la clase Usuario
        infractor.registrarReporteFraudeValidado();

        repositorioUsuario.guardar(infractor);
    }

    /**
     * Mantenimiento de Catálogo: Agregar nuevas categorías globales de Habilidades.
     */
    public void agregarNuevaHabilidadAlCatalogoGlobal(String idAdministrador, String nombreCategoria) {
        verificarPermisosAdmin(idAdministrador);

        // Delegamos la creación al servicio de habilidades que ya tenías
        servicioHabilidad.crearHabilidad(nombreCategoria);
    }

    // --- FUNCIÓN DE SEGURIDAD INTERNA ---
    private void verificarPermisosAdmin(String idUsuario) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new SecurityException("Usuario no autenticado."));

        if (!usuario.isAdministrador()) {
            throw new SecurityException("Acceso denegado: Esta acción requiere privilegios de Administrador.");
        }
    }
}