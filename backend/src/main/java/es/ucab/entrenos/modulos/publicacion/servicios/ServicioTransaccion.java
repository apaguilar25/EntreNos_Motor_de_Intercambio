package es.ucab.entrenos.modulos.publicacion.servicios;

import es.ucab.entrenos.modulos.gamificacion.modelos.LogroDesbloqueado;
import es.ucab.entrenos.modulos.gamificacion.servicios.ServicioGamificacion;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.publicacion.dtos.ConfirmacionTransaccionResponseDTO;
import es.ucab.entrenos.modulos.reputacion.servicios.ServicioReputacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Cancelacion;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoCancelacion;
import es.ucab.entrenos.modulos.publicacion.modelos.MotivoCancelacion;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoTransaccion;
import es.ucab.entrenos.modulos.publicacion.modelos.Incidencia;
import es.ucab.entrenos.modulos.reputacion.modelos.Resena;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioCancelacion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioIncidencia;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioPublicacion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioTransaccion {
    private final IRepositorioTransaccion repositorioTransaccion;
    private final IRepositorioPublicacion repositorioPublicacion;
    private final IRepositorioIncidencia repositorioIncidencia;
    private final IRepositorioCancelacion repositorioCancelacion;
    private final ServicioUsuario servicioUsuario;
    private final ServicioNotificacion servicioNotificacion;
    private final ServicioGamificacion servicioGamificacion;
    private final ServicioReputacion servicioReputacion;

    private static final Logger log = LoggerFactory.getLogger(ServicioTransaccion.class);

    public ServicioTransaccion(IRepositorioTransaccion repositorioTransaccion,
                                IRepositorioPublicacion repositorioPublicacion,
                                IRepositorioIncidencia repositorioIncidencia,
                                IRepositorioCancelacion repositorioCancelacion,
                                ServicioUsuario servicioUsuario,
                                ServicioNotificacion servicioNotificacion,
                                ServicioGamificacion servicioGamificacion,
                                ServicioReputacion servicioReputacion) {
        this.repositorioTransaccion = repositorioTransaccion;
        this.repositorioPublicacion = repositorioPublicacion;
        this.repositorioIncidencia = repositorioIncidencia;
        this.repositorioCancelacion = repositorioCancelacion;
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
        this.servicioGamificacion = servicioGamificacion;
        this.servicioReputacion = servicioReputacion;
    }

    public List<Transaccion> obtenerTodas() {
        return repositorioTransaccion.obtenerTodas();
    }

    public Optional<Transaccion> obtenerPorId(String id) {
        return repositorioTransaccion.obtenerPorId(id);
    }

    public Transaccion crear(Transaccion transaccion) {
        if (transaccion.getIdTransaccion() == null || transaccion.getIdTransaccion().isEmpty()) {
            transaccion.setIdTransaccion("TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        transaccion.setFechaCreacion(System.currentTimeMillis());
        repositorioTransaccion.guardar(transaccion);
        return transaccion;
    }

    public ConfirmacionTransaccionResponseDTO confirmarOfertante(String idTransaccion) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        Publicacion pub = repositorioPublicacion.obtenerPorId(t.getIdPublicacion())
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada."));
        EstadoTransaccion estadoAnterior = t.getEstado();
        t.confirmarEntregaOfertante();
        if (t.getEstado() == EstadoTransaccion.FINALIZADA && estadoAnterior != EstadoTransaccion.FINALIZADA) {
            procesarPago(t);
        }
        repositorioTransaccion.guardar(t);
        List<LogroDesbloqueado> nuevosLogros = new ArrayList<>();
        if (t.getEstado() == EstadoTransaccion.FINALIZADA) {
            nuevosLogros.addAll(servicioGamificacion.evaluarLogros(t.getIdOfertante()));
            nuevosLogros.addAll(servicioGamificacion.evaluarLogros(t.getIdDemandante()));
        }
        servicioNotificacion.enviarNotificacion(t.getIdOfertante(), t.getIdDemandante(),
                "El ofertante confirmó la entrega del servicio: " + pub.getNombreServicio(),
                TipoNotificacion.TRANSACCION_ACTUALIZADA);
        return new ConfirmacionTransaccionResponseDTO(t, nuevosLogros);
    }

    public ConfirmacionTransaccionResponseDTO confirmarDemandante(String idTransaccion) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        Publicacion pub = repositorioPublicacion.obtenerPorId(t.getIdPublicacion())
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada."));
        EstadoTransaccion estadoAnterior = t.getEstado();
        t.confirmarRecepcionDemandante();
        if (t.getEstado() == EstadoTransaccion.FINALIZADA && estadoAnterior != EstadoTransaccion.FINALIZADA) {
            procesarPago(t);
        }
        repositorioTransaccion.guardar(t);
        List<LogroDesbloqueado> nuevosLogros = new ArrayList<>();
        if (t.getEstado() == EstadoTransaccion.FINALIZADA) {
            nuevosLogros.addAll(servicioGamificacion.evaluarLogros(t.getIdOfertante()));
            nuevosLogros.addAll(servicioGamificacion.evaluarLogros(t.getIdDemandante()));
        }
        servicioNotificacion.enviarNotificacion(t.getIdDemandante(), t.getIdOfertante(),
                "El demandante confirmó la recepción del servicio: " + pub.getNombreServicio(),
                TipoNotificacion.TRANSACCION_ACTUALIZADA);
        return new ConfirmacionTransaccionResponseDTO(t, nuevosLogros);
    }

    private void procesarPago(Transaccion t) {
        Usuario ofertante = servicioUsuario.buscarPorId(t.getIdOfertante())
                .orElseThrow(() -> new IllegalArgumentException("Ofertante no encontrado."));
        Usuario demandante = servicioUsuario.buscarPorId(t.getIdDemandante())
                .orElseThrow(() -> new IllegalArgumentException("Demandante no encontrado."));
        Publicacion pub = repositorioPublicacion.obtenerPorId(t.getIdPublicacion())
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada."));
        demandante.getMonedero().liberarCompromiso();
        demandante.incrementarVersion();
        ofertante.getMonedero().acreditar(t.getCreditosRetenidos());
        ofertante.incrementarVersion();
        servicioUsuario.guardar(demandante);
        servicioUsuario.guardar(ofertante);
        servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdOfertante(),
                "Recibiste " + t.getCreditosRetenidos() + " créditos por el servicio: " + pub.getNombreServicio(),
                TipoNotificacion.MOVIMIENTO_MONEDERO);
        servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdDemandante(),
                "Se liberaron " + t.getCreditosRetenidos() + " créditos comprometidos por: " + pub.getNombreServicio(),
                TipoNotificacion.MOVIMIENTO_MONEDERO);
    }

    public Transaccion calificar(String idTransaccion, String idUsuario, int calificacion) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        if (t.getEstado() != EstadoTransaccion.FINALIZADA) {
            throw new IllegalStateException("Solo se puede calificar una transacción finalizada.");
        }
        if (calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5.");
        }
        if (!t.getIdDemandante().equals(idUsuario)) {
            throw new IllegalArgumentException("Solo el demandante puede calificar el servicio.");
        }
        if (!servicioReputacion.obtenerResenasPorTransaccion(idTransaccion).isEmpty()) {
            throw new IllegalStateException("Ya calificaste esta transacción.");
        }
        Resena resena = servicioReputacion.crearResena(idTransaccion, idUsuario, t.getIdOfertante(), calificacion);
        t.setResena(resena);
        repositorioTransaccion.guardar(t);
        return t;
    }

    public Incidencia reportarIncidencia(String idTransaccion, String idUsuario, String descripcion, String urlEvidencia) {
        return reportarIncidencia(idTransaccion, idUsuario, descripcion, urlEvidencia, null);
    }

    public Incidencia reportarIncidencia(String idTransaccion, String idUsuario, String descripcion, String urlEvidencia, java.util.List<String> fotosEvidencia) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        if (descripcion == null || descripcion.trim().length() < 20) {
            throw new IllegalArgumentException("La descripción del incidente debe tener al menos 20 caracteres.");
        }
        Incidencia incidencia = new Incidencia(idTransaccion, idUsuario, descripcion, urlEvidencia, fotosEvidencia);
        repositorioIncidencia.guardar(incidencia);
        t.asignarIncidencia(incidencia.getIdIncidencia());
        repositorioTransaccion.guardar(t);
        Publicacion pub = repositorioPublicacion.obtenerPorId(t.getIdPublicacion())
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada."));
        servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdOfertante(),
                "Se ha reportado una incidencia en la transacción \"" + pub.getNombreServicio()
                        + "\". El intercambio ha sido marcado bajo revisión. Los créditos permanecen bloqueados.",
                TipoNotificacion.ALERTA_SISTEMA);
        servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdDemandante(),
                "Se ha reportado una incidencia en la transacción \"" + pub.getNombreServicio()
                        + "\". El intercambio ha sido marcado bajo revisión. Los créditos permanecen bloqueados.",
                TipoNotificacion.ALERTA_SISTEMA);
        return incidencia;
    };

    public Incidencia defenderIncidencia(String idTransaccion, String idUsuario, String descripcion, String urlEvidencia) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        if (!t.getIdOfertante().equals(idUsuario) && !t.getIdDemandante().equals(idUsuario)) {
            throw new IllegalArgumentException("No eres parte de esta transacción.");
        }
        if (t.getIdIncidencia() == null) {
            throw new IllegalStateException("No hay una incidencia activa en esta transacción.");
        }
        Incidencia incidencia = repositorioIncidencia.obtenerPorId(t.getIdIncidencia())
                .orElseThrow(() -> new IllegalStateException("La incidencia asociada no existe."));
        if (incidencia.getIdUsuarioReportante().equals(idUsuario)) {
            throw new IllegalArgumentException("No puedes defenderte de tu propio reporte.");
        }
        if (incidencia.getIdUsuarioDefensor() != null) {
            throw new IllegalStateException("Ya has presentado tu defensa para esta incidencia.");
        }
        if (descripcion == null || descripcion.trim().length() < 20) {
            throw new IllegalArgumentException("La descripción de la defensa debe tener al menos 20 caracteres.");
        }
        incidencia.setIdUsuarioDefensor(idUsuario);
        incidencia.setDescripcionDefensa(descripcion);
        incidencia.setUrlEvidenciaDefensa(urlEvidencia);
        repositorioIncidencia.guardar(incidencia);
        Publicacion pub = repositorioPublicacion.obtenerPorId(t.getIdPublicacion())
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada."));
        servicioNotificacion.enviarNotificacion(idUsuario, incidencia.getIdUsuarioReportante(),
                "El usuario implicado ha presentado su defensa para la incidencia en: " + pub.getNombreServicio(),
                TipoNotificacion.ALERTA_SISTEMA);
        return incidencia;
    }

    public Cancelacion solicitarCancelacion(String idTransaccion, String idUsuario, String idMotivoCancelacion) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        if (!t.getIdOfertante().equals(idUsuario) && !t.getIdDemandante().equals(idUsuario)) {
            throw new IllegalArgumentException("No eres parte de esta transacción.");
        }
        if (t.getEstado() == EstadoTransaccion.FINALIZADA || t.getEstado() == EstadoTransaccion.RECHAZADA || t.getEstado() == EstadoTransaccion.EN_DISPUTA) {
            throw new IllegalStateException("No se puede solicitar cancelación en estado " + t.getEstado() + ".");
        }
        MotivoCancelacion motivo;
        try {
            motivo = MotivoCancelacion.valueOf(idMotivoCancelacion);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Motivo de cancelación no válido: " + idMotivoCancelacion);
        }
        Optional<Cancelacion> pendiente = repositorioCancelacion.obtenerTodas().stream()
                .filter(c -> c.getIdTransaccion().equals(idTransaccion)
                        && c.getEstado() == EstadoCancelacion.PENDIENTE)
                .findFirst();
        if (pendiente.isPresent()) {
            Cancelacion existente = pendiente.get();
            if (existente.getIdSolicitante().equals(idUsuario)) {
                throw new IllegalStateException("Ya solicitaste la cancelación de esta transacción.");
            }
            existente.aceptar();
            repositorioCancelacion.guardar(existente);
            procesarCancelacionAceptada(t);
            Publicacion pub = repositorioPublicacion.obtenerPorId(t.getIdPublicacion()).orElse(null);
            String nombreServicio = pub != null ? pub.getNombreServicio() : "Servicio";
            servicioNotificacion.enviarNotificacion("SISTEMA", existente.getIdSolicitante(),
                    "Ambas partes solicitaron la cancelación de \"" + nombreServicio + "\". La transacción ha sido cancelada.",
                    TipoNotificacion.ALERTA_SISTEMA);
            servicioNotificacion.enviarNotificacion("SISTEMA", idUsuario,
                    "Ambas partes solicitaron la cancelación de \"" + nombreServicio + "\". La transacción ha sido cancelada.",
                    TipoNotificacion.ALERTA_SISTEMA);
            return existente;
        }
        String contraparte = t.getIdOfertante().equals(idUsuario) ? t.getIdDemandante() : t.getIdOfertante();
        Cancelacion cancelacion = new Cancelacion(idTransaccion, idUsuario, contraparte, motivo);
        repositorioCancelacion.guardar(cancelacion);
        Publicacion pub = repositorioPublicacion.obtenerPorId(t.getIdPublicacion()).orElse(null);
        String nombreServicio = pub != null ? pub.getNombreServicio() : "Servicio";
        servicioNotificacion.enviarNotificacion(idUsuario, contraparte,
                "Se ha solicitado la cancelación de \"" + nombreServicio + "\". Motivo: " + motivo.getDescripcion(),
                TipoNotificacion.ALERTA_SISTEMA);
        return cancelacion;
    }

    public Cancelacion responderCancelacion(String idTransaccion, String idUsuario, boolean aceptar) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        Cancelacion cancelacion = repositorioCancelacion.obtenerTodas().stream()
                .filter(c -> c.getIdTransaccion().equals(idTransaccion)
                        && c.getEstado() == EstadoCancelacion.PENDIENTE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay una solicitud de cancelación pendiente."));
        if (cancelacion.getIdSolicitante().equals(idUsuario)) {
            throw new IllegalArgumentException("No puedes responder tu propia solicitud de cancelación.");
        }
        if (!t.getIdOfertante().equals(idUsuario) && !t.getIdDemandante().equals(idUsuario)) {
            throw new IllegalArgumentException("No eres parte de esta transacción.");
        }
        if (aceptar) {
            cancelacion.aceptar();
            repositorioCancelacion.guardar(cancelacion);
            procesarCancelacionAceptada(t);
        } else {
            cancelacion.rechazar();
            repositorioCancelacion.guardar(cancelacion);
        }
        Publicacion pub = repositorioPublicacion.obtenerPorId(t.getIdPublicacion()).orElse(null);
        String nombreServicio = pub != null ? pub.getNombreServicio() : "Servicio";
        String contraparte = t.getIdOfertante().equals(idUsuario) ? t.getIdDemandante() : t.getIdOfertante();
        if (aceptar) {
            servicioNotificacion.enviarNotificacion(idUsuario, contraparte,
                    "La cancelación de \"" + nombreServicio + "\" ha sido aceptada. La transacción ha sido cancelada.",
                    TipoNotificacion.ALERTA_SISTEMA);
        } else {
            servicioNotificacion.enviarNotificacion(idUsuario, contraparte,
                    "La cancelación de \"" + nombreServicio + "\" ha sido rechazada. La transacción continúa.",
                    TipoNotificacion.ALERTA_SISTEMA);
        }
        return cancelacion;
    }

    private void procesarCancelacionAceptada(Transaccion t) {
        if (t.getEstado() == EstadoTransaccion.INICIADA) {
            Usuario demandante = servicioUsuario.buscarPorId(t.getIdDemandante())
                    .orElseThrow(() -> new IllegalArgumentException("Demandante no encontrado."));
            demandante.getMonedero().devolverCompromiso();
            demandante.incrementarVersion();
            servicioUsuario.guardar(demandante);
        }
        t.setEstado(EstadoTransaccion.RECHAZADA);
        repositorioTransaccion.guardar(t);
    }

    public List<Transaccion> obtenerCalificacionesPorPublicacion(String idPublicacion) {
        return repositorioTransaccion.obtenerTodas().stream()
                .filter(t -> t.getIdPublicacion().equals(idPublicacion)
                        && t.getEstado() == EstadoTransaccion.FINALIZADA)
                .collect(Collectors.toList());
    }
}
