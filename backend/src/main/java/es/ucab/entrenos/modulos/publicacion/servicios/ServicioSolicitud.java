package es.ucab.entrenos.modulos.publicacion.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoTransaccion;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Solicitud;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioSolicitud;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioSolicitud {

    private final IRepositorioSolicitud repositorioSolicitud;
    private final ServicioPublicacion servicioPublicacion;
    private final ServicioUsuario servicioUsuario;
    private final ServicioNotificacion servicioNotificacion;
    private final IRepositorioTransaccion repositorioTransaccion;

    public ServicioSolicitud(IRepositorioSolicitud repositorioSolicitud,
                             ServicioPublicacion servicioPublicacion,
                             ServicioUsuario servicioUsuario,
                             ServicioNotificacion servicioNotificacion,
                             IRepositorioTransaccion repositorioTransaccion) {
        this.repositorioSolicitud = repositorioSolicitud;
        this.servicioPublicacion = servicioPublicacion;
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
        this.repositorioTransaccion = repositorioTransaccion;
    }

    public List<Solicitud> obtenerTodas() {
        return repositorioSolicitud.obtenerTodas();
    }

    public Optional<Solicitud> obtenerPorId(String id) {
        return repositorioSolicitud.obtenerPorId(id);
    }

    public List<Solicitud> obtenerPorPublicacion(String idPublicacion) {
        return repositorioSolicitud.obtenerTodas().stream()
                .filter(s -> s.getIdPublicacion().equalsIgnoreCase(idPublicacion))
                .collect(Collectors.toList());
    }

    public Solicitud cancelar(String idSolicitud, String idUsuario) {
        Solicitud solicitud = repositorioSolicitud.obtenerPorId(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + idSolicitud));
        if (!solicitud.getIdSolicitante().equals(idUsuario)) {
            throw new IllegalArgumentException("Solo el solicitante puede cancelar la solicitud.");
        }

        Publicacion pub = servicioPublicacion.obtenerPublicacionPorId(solicitud.getIdPublicacion())
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada."));

        if (Solicitud.ESTADO_PENDIENTE.equals(solicitud.getEstado()) && pub.getTipoPublicacion().equals("HABILIDAD") && pub.getPrecioCreditos() > 0) {
            Usuario solicitante = servicioUsuario.buscarPorId(idUsuario)
                    .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
            solicitante.getMonedero().devolverCompromiso();
            solicitante.incrementarVersion();
            servicioUsuario.guardar(solicitante);
        }

        solicitud.cancelar();
        repositorioSolicitud.guardar(solicitud);
        return solicitud;
    }

    public List<Solicitud> obtenerPorSolicitante(String idSolicitante, String tipo) {
        return repositorioSolicitud.obtenerTodas().stream()
                .filter(s -> s.getIdSolicitante().equalsIgnoreCase(idSolicitante))
                .filter(s -> tipo == null || tipo.isEmpty() ||
                        servicioPublicacion.obtenerPublicacionPorId(s.getIdPublicacion())
                                .map(p -> p.getTipoPublicacion().equalsIgnoreCase(tipo))
                                .orElse(false))
                .collect(Collectors.toList());
    }

    public List<Solicitud> obtenerRecibidas(String idPropietario, String tipo) {
        List<Publicacion> pubs = servicioPublicacion.obtenerTodasLasPublicaciones().stream()
                .filter(p -> p.getIdUsuario().equals(idPropietario))
                .filter(p -> tipo == null || tipo.isEmpty() || p.getTipoPublicacion().equalsIgnoreCase(tipo))
                .collect(Collectors.toList());
        return repositorioSolicitud.obtenerTodas().stream()
                .filter(s -> pubs.stream().anyMatch(p -> p.getIdPublicacion().equals(s.getIdPublicacion())))
                .collect(Collectors.toList());
    }

    public Solicitud solicitar(String idPublicacion, String idSolicitante) {
        Publicacion pub = servicioPublicacion.obtenerPublicacionPorId(idPublicacion)
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada: " + idPublicacion));

        if (pub.getIdUsuario().equals(idSolicitante)) {
            throw new IllegalArgumentException("No puedes solicitar tu propia publicacion.");
        }
        if (!pub.isDisponible()) {
            throw new IllegalStateException("La publicacion no esta disponible.");
        }

        if (pub.getTipoPublicacion().equals("HABILIDAD") && pub.getPrecioCreditos() > 0) {
            Usuario solicitante = servicioUsuario.buscarPorId(idSolicitante)
                    .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
            if (solicitante.getMonedero().getSaldoDisponible() < pub.getPrecioCreditos()) {
                throw new IllegalStateException("Saldo insuficiente. Necesitas " + pub.getPrecioCreditos()
                        + " creditos, pero tienes " + solicitante.getMonedero().getSaldoDisponible() + " disponibles.");
            }
            solicitante.getMonedero().comprometer(pub.getPrecioCreditos());
            solicitante.incrementarVersion();
            servicioUsuario.guardar(solicitante);
        }

        Solicitud solicitud = new Solicitud(idPublicacion, idSolicitante);
        repositorioSolicitud.guardar(solicitud);

        String nombreSolicitante = servicioUsuario.buscarPorId(idSolicitante)
                .map(Usuario::getNombre).orElse("Un usuario");
        servicioNotificacion.enviarNotificacion(idSolicitante, pub.getIdUsuario(),
                nombreSolicitante + " quiere contratar tu servicio: " + pub.getNombreServicio(),
                TipoNotificacion.NUEVA_SOLICITUD_ENTRANTE, solicitud.getIdSolicitud(), pub.getIdPublicacion());

        servicioNotificacion.enviarNotificacion("SISTEMA", idSolicitante,
                "Has ofertado exitosamente por: " + pub.getNombreServicio() + ". Esperando respuesta del proveedor.",
                TipoNotificacion.ALERTA_SISTEMA, solicitud.getIdSolicitud(), pub.getIdPublicacion());

        return solicitud;
    }

    public Solicitud responder(String idSolicitud, String idUsuario, boolean aceptar) {
        Solicitud solicitud = repositorioSolicitud.obtenerPorId(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + idSolicitud));

        Publicacion pub = servicioPublicacion.obtenerPublicacionPorId(solicitud.getIdPublicacion())
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada."));

        if (!pub.getIdUsuario().equals(idUsuario)) {
            throw new IllegalArgumentException("Solo el dueno de la publicacion puede responder la solicitud.");
        }

        if (solicitud.haExpirado()) {
            if (Solicitud.ESTADO_PENDIENTE.equals(solicitud.getEstado()) && pub.getTipoPublicacion().equals("HABILIDAD") && pub.getPrecioCreditos() > 0) {
                Usuario solicitante = servicioUsuario.buscarPorId(solicitud.getIdSolicitante())
                        .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
                solicitante.getMonedero().devolverCompromiso();
                solicitante.incrementarVersion();
                servicioUsuario.guardar(solicitante);
            }
            solicitud.expirar();
            repositorioSolicitud.guardar(solicitud);
            String nombreSolicitante = servicioUsuario.buscarPorId(solicitud.getIdSolicitante())
                    .map(Usuario::getNombre).orElse("Un usuario");
            servicioNotificacion.enviarNotificacion("SISTEMA", pub.getIdUsuario(),
                    "La solicitud de " + nombreSolicitante + " para " + pub.getNombreServicio()
                            + " ha expirado automaticamente.", TipoNotificacion.ALERTA_SISTEMA);
            servicioNotificacion.enviarNotificacion("SISTEMA", solicitud.getIdSolicitante(),
                    "Tu solicitud para " + pub.getNombreServicio() + " ha expirado (sin respuesta del dueno).",
                    TipoNotificacion.ALERTA_SISTEMA);
            throw new IllegalStateException("El plazo de respuesta ha expirado.");
        }

        if (aceptar) {
            boolean yaTieneTransaccionActiva = repositorioTransaccion.obtenerTodas().stream()
                    .anyMatch(t -> t.getIdPublicacion().equals(solicitud.getIdPublicacion())
                            && (t.getEstado() == EstadoTransaccion.PENDIENTE || t.getEstado() == EstadoTransaccion.INICIADA || t.getEstado() == EstadoTransaccion.EN_DISPUTA));
            if (yaTieneTransaccionActiva) {
                throw new IllegalStateException("Esta publicación ya tiene una transacción activa.");
            }
            solicitud.aceptar();

            Transaccion tx = new Transaccion(
                pub.getIdPublicacion(),
                pub.getIdUsuario(),
                solicitud.getIdSolicitante(),
                pub.getPrecioCreditos()
            );
            repositorioTransaccion.guardar(tx);

            servicioNotificacion.enviarNotificacion(pub.getIdUsuario(), solicitud.getIdSolicitante(),
                    "Tu solicitud para " + pub.getNombreServicio() + " fue ACEPTADA. Se retuvieron "
                            + pub.getPrecioCreditos() + " creditos.",
                    TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);
        } else {
            if (Solicitud.ESTADO_PENDIENTE.equals(solicitud.getEstado()) && pub.getTipoPublicacion().equals("HABILIDAD") && pub.getPrecioCreditos() > 0) {
                Usuario solicitante = servicioUsuario.buscarPorId(solicitud.getIdSolicitante())
                        .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
                solicitante.getMonedero().devolverCompromiso();
                solicitante.incrementarVersion();
                servicioUsuario.guardar(solicitante);
            }
            solicitud.rechazar();

            servicioNotificacion.enviarNotificacion(pub.getIdUsuario(), solicitud.getIdSolicitante(),
                    "Tu solicitud para " + pub.getNombreServicio() + " fue RECHAZADA.",
                    TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);
        }

        repositorioSolicitud.guardar(solicitud);
        return solicitud;
    }
}
