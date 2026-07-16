package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioCorreoPermitido;
import es.ucab.entrenos.modulos.identidad.modelos.CorreoPermitido;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoTransaccion;
import es.ucab.entrenos.modulos.publicacion.modelos.Incidencia;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioIncidencia;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioAdministrador {

    private final IRepositorioUsuario repositorioUsuario;
    private final IRepositorioCorreoPermitido repositorioCorreoPermitido;
    private final ServicioHabilidad servicioHabilidad;
    private final ServicioAutenticacion servicioAutenticacion;
    private final IRepositorioIncidencia repositorioIncidencia;
    private final IRepositorioTransaccion repositorioTransaccion;

    public ServicioAdministrador(IRepositorioUsuario repositorioUsuario, 
                                  IRepositorioCorreoPermitido repositorioCorreoPermitido,
                                  ServicioHabilidad servicioHabilidad,
                                  ServicioAutenticacion servicioAutenticacion,
                                  IRepositorioIncidencia repositorioIncidencia,
                                  IRepositorioTransaccion repositorioTransaccion) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioCorreoPermitido = repositorioCorreoPermitido;
        this.servicioHabilidad = servicioHabilidad;
        this.servicioAutenticacion = servicioAutenticacion;
        this.repositorioIncidencia = repositorioIncidencia;
        this.repositorioTransaccion = repositorioTransaccion;
    }

    public List<Incidencia> listarIncidencias() {
        return repositorioIncidencia.obtenerTodas();
    }

    public Optional<Incidencia> obtenerIncidenciaPorId(String idIncidencia) {
        return repositorioIncidencia.obtenerPorId(idIncidencia);
    }

    public void resolverIncidencia(String idAdministrador, String idIncidencia, String idUsuarioGanadorCreditos, boolean sancionarOfertante, boolean sancionarDemandante) {
        resolverIncidencia(idAdministrador, idIncidencia, idUsuarioGanadorCreditos, sancionarOfertante, sancionarDemandante, false, false);
    }

    public void resolverIncidencia(String idAdministrador, String idIncidencia, String idUsuarioGanadorCreditos, boolean sancionarOfertante, boolean sancionarDemandante, Boolean sancionarReportante, Boolean sancionarDefensor) {
        verificarPermisosAdmin(idAdministrador);

        Incidencia incidencia = repositorioIncidencia.obtenerPorId(idIncidencia)
                .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada."));

        if (!incidencia.getEstado().equals("ABIERTA")) {
            throw new IllegalStateException("Esta incidencia ya ha sido resuelta.");
        }

        Transaccion t = repositorioTransaccion.obtenerPorId(incidencia.getIdTransaccion())
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada."));

        // Resolver pago
        Usuario ofertante = repositorioUsuario.buscarPorId(t.getIdOfertante()).orElseThrow();
        Usuario demandante = repositorioUsuario.buscarPorId(t.getIdDemandante()).orElseThrow();

        if (idUsuarioGanadorCreditos.equals(t.getIdOfertante())) {
            // Entregar fondos al ofertante
            demandante.getMonedero().liberarCompromiso();
            ofertante.getMonedero().acreditar(t.getCreditosRetenidos());
        } else if (idUsuarioGanadorCreditos.equals(t.getIdDemandante())) {
            // Devolver fondos al demandante
            demandante.getMonedero().devolverCompromiso();
        } else {
            throw new IllegalArgumentException("El ganador debe ser el ofertante o el demandante de la transacción.");
        }

        // Mapear sancionarReportante/sancionarDefensor a ofertante/demandante
        if (sancionarReportante != null && sancionarReportante) {
            String idReportante = incidencia.getIdUsuarioReportante();
            if (idReportante.equals(t.getIdOfertante())) {
                ofertante.registrarReporteFraudeValidado();
            } else if (idReportante.equals(t.getIdDemandante())) {
                demandante.registrarReporteFraudeValidado();
            }
        }
        if (sancionarDefensor != null && sancionarDefensor) {
            String idDefensor = incidencia.getIdUsuarioDefensor();
            if (idDefensor != null) {
                if (idDefensor.equals(t.getIdOfertante())) {
                    ofertante.registrarReporteFraudeValidado();
                } else if (idDefensor.equals(t.getIdDemandante())) {
                    demandante.registrarReporteFraudeValidado();
                }
            }
        }

        // Sancionar (legado)
        if (sancionarOfertante) {
            ofertante.registrarReporteFraudeValidado();
        }
        if (sancionarDemandante) {
            demandante.registrarReporteFraudeValidado();
        }

        // Guardar usuarios
        ofertante.incrementarVersion();
        demandante.incrementarVersion();
        repositorioUsuario.guardar(ofertante);
        repositorioUsuario.guardar(demandante);

        // Finalizar transacción e incidencia
        t.setEstado(EstadoTransaccion.FINALIZADA);
        repositorioTransaccion.guardar(t);

        incidencia.setEstado("RESUELTA");
        repositorioIncidencia.guardar(incidencia);
    }

    /**
     * Mantenimiento de Catálogo: Agregar nuevas categorías globales de Habilidades.
     */
    public void agregarNuevaHabilidadAlCatalogoGlobal(String idAdministrador, String nombreCategoria) {
        verificarPermisosAdmin(idAdministrador);

        // Delegamos la creación al servicio de habilidades que ya tenías
        servicioHabilidad.crearHabilidad(nombreCategoria);
    }

    // --- GESTIÓN DE CORREOS PERMITIDOS ---
    public List<CorreoPermitido> listarCorreosPermitidos(String idAdministrador) {
        verificarPermisosAdmin(idAdministrador);
        return repositorioCorreoPermitido.obtenerTodos();
    }

    public void agregarCorreoPermitido(String idAdministrador, String correo) {
        verificarPermisosAdmin(idAdministrador);
        repositorioCorreoPermitido.guardar(new CorreoPermitido(correo));
    }

    public void eliminarCorreoPermitido(String idAdministrador, String correo) {
        verificarPermisosAdmin(idAdministrador);
        repositorioCorreoPermitido.eliminar(correo);
    }

    // --- GESTIÓN DE USUARIOS ---
    public List<Usuario> listarUsuarios(String idAdministrador) {
        verificarPermisosAdmin(idAdministrador);
        return repositorioUsuario.listarUsuarios();
    }

    public void modificarCreditosUsuario(String idAdministrador, String idUsuarioObjetivo, float nuevosCreditos) {
        verificarPermisosAdmin(idAdministrador);
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuarioObjetivo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        
        float diferencia = nuevosCreditos - usuario.getMonedero().getCreditosDisponibles();
        if (diferencia > 0) {
            usuario.getMonedero().acreditar(diferencia);
        } else if (diferencia < 0) {
            usuario.getMonedero().descontar(Math.abs(diferencia));
        }
        usuario.incrementarVersion();
        repositorioUsuario.guardar(usuario);
    }

    public void perdonarFaltas(String idAdministrador, String idUsuarioObjetivo) {
        verificarPermisosAdmin(idAdministrador);
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuarioObjetivo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        
        usuario.perdonarFaltas();
        usuario.incrementarVersion();
        repositorioUsuario.guardar(usuario);
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