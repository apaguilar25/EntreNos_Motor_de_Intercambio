package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import es.ucab.entrenos.modulos.publicacion.modelos.Incidencia;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioIncidencia;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioAdministrador {

    private final IRepositorioUsuario repositorioUsuario;
    private final ServicioHabilidad servicioHabilidad;
    private final ServicioAutenticacion servicioAutenticacion;
    private final IRepositorioIncidencia repositorioIncidencia;
    private final IRepositorioTransaccion repositorioTransaccion;

    public ServicioAdministrador(IRepositorioUsuario repositorioUsuario, ServicioHabilidad servicioHabilidad,
                                  ServicioAutenticacion servicioAutenticacion,
                                  IRepositorioIncidencia repositorioIncidencia,
                                  IRepositorioTransaccion repositorioTransaccion) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicioHabilidad = servicioHabilidad;
        this.servicioAutenticacion = servicioAutenticacion;
        this.repositorioIncidencia = repositorioIncidencia;
        this.repositorioTransaccion = repositorioTransaccion;
    }

    public List<Incidencia> listarIncidencias() {
        return repositorioIncidencia.obtenerTodas();
    }

    /**
     * Requisito ERS: Validar reporte de fraude y aplicar sanción.
     */
    public void validarReporteDeFraude(String idAdministrador, String idUsuarioInfractor, String idTransaccion) {
        verificarPermisosAdmin(idAdministrador);

        Usuario infractor = repositorioUsuario.buscarPorId(idUsuarioInfractor)
                .orElseThrow(() -> new IllegalArgumentException("Usuario infractor no encontrado."));

        infractor.registrarReporteFraudeValidado();
        repositorioUsuario.guardar(infractor);

        if (idTransaccion != null && !idTransaccion.isBlank()) {
            repositorioTransaccion.obtenerPorId(idTransaccion).ifPresent(t -> {
                t.setSancionado(true);
                repositorioTransaccion.guardar(t);
            });
        }
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