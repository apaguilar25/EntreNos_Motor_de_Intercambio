package es.ucab.entrenos.modulos.publicacion.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoSolicitud;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoTransaccion;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Solicitud;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.identidad.modelos.HabilidadOfrecida;
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

        if (solicitud.getEstado() == EstadoSolicitud.PENDIENTE && pub.getTipoPublicacion().equals("HABILIDAD") && pub.getPrecioCreditos() > 0) {
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

    public Solicitud solicitar(String idPublicacion, String idSolicitante, Integer precioOfertado) {
        Publicacion pub = servicioPublicacion.obtenerPublicacionPorId(idPublicacion)
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada: " + idPublicacion));

        if (pub.getIdUsuario().equals(idSolicitante)) {
            throw new IllegalArgumentException("No puedes solicitar tu propia publicacion.");
        }
        if (!pub.isDisponible()) {
            throw new IllegalStateException("La publicacion no esta disponible.");
        }

        int precioFinal;
        String tipoNotif;

        if (pub.getTipoPublicacion().equals("NECESIDAD")) {
            if (precioOfertado != null) {
                precioFinal = precioOfertado;
            } else {
                Usuario solicitante = servicioUsuario.buscarPorId(idSolicitante)
                        .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
                HabilidadOfrecida hab = solicitante.getHabilidadesOfrecidas().stream()
                        .filter(h -> h.getHabilidadBase().getCategoria().equalsIgnoreCase(pub.getNombreServicio()))
                        .findFirst().orElse(null);
                precioFinal = (hab != null) ? hab.getPrecioCreditos() : 0;
            }
            tipoNotif = "ofrecer servicio";
        } else {
            precioFinal = pub.getPrecioCreditos();
            tipoNotif = "contratar servicio";
        }

        if (pub.getTipoPublicacion().equals("HABILIDAD") && precioFinal > 0) {
            Usuario solicitante = servicioUsuario.buscarPorId(idSolicitante)
                    .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
            if (solicitante.getMonedero().getSaldoDisponible() < precioFinal) {
                throw new IllegalStateException("Saldo insuficiente. Necesitas " + precioFinal
                        + " creditos, pero tienes " + solicitante.getMonedero().getSaldoDisponible() + " disponibles.");
            }
            solicitante.getMonedero().comprometer(precioFinal);
            solicitante.incrementarVersion();
            servicioUsuario.guardar(solicitante);
        }

        Solicitud solicitud = new Solicitud(idPublicacion, idSolicitante, precioFinal);
        repositorioSolicitud.guardar(solicitud);

        String nombreSolicitante = servicioUsuario.buscarPorId(idSolicitante)
                .map(Usuario::getNombre).orElse("Un usuario");

        if (pub.getTipoPublicacion().equals("NECESIDAD")) {
            servicioNotificacion.enviarNotificacion(idSolicitante, pub.getIdUsuario(),
                    nombreSolicitante + " quiere cubrir tu necesidad de " + pub.getNombreServicio()
                            + " por " + precioFinal + " créditos.",
                    TipoNotificacion.NUEVA_SOLICITUD_ENTRANTE, solicitud.getIdSolicitud(), pub.getIdPublicacion());
            servicioNotificacion.enviarNotificacion("SISTEMA", idSolicitante,
                    "Has ofertado tus servicios para: " + pub.getNombreServicio() + " por " + precioFinal
                            + " créditos. Esperando respuesta.",
                    TipoNotificacion.ALERTA_SISTEMA, solicitud.getIdSolicitud(), pub.getIdPublicacion());
        } else {
            servicioNotificacion.enviarNotificacion(idSolicitante, pub.getIdUsuario(),
                    nombreSolicitante + " quiere " + tipoNotif + ": " + pub.getNombreServicio() + " por " + precioFinal + " créditos.",
                    TipoNotificacion.NUEVA_SOLICITUD_ENTRANTE, solicitud.getIdSolicitud(), pub.getIdPublicacion());
            servicioNotificacion.enviarNotificacion("SISTEMA", idSolicitante,
                    "Has ofertado exitosamente por: " + pub.getNombreServicio() + " por " + precioFinal + " créditos. Esperando respuesta del proveedor.",
                    TipoNotificacion.ALERTA_SISTEMA, solicitud.getIdSolicitud(), pub.getIdPublicacion());
        }

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

        int precio = solicitud.getPrecioOfertado();
        boolean esNecesidad = pub.getTipoPublicacion().equals("NECESIDAD");

        if (solicitud.haExpirado()) {
            if (solicitud.getEstado() == EstadoSolicitud.PENDIENTE) {
                if (pub.getTipoPublicacion().equals("HABILIDAD") && precio > 0) {
                    Usuario solicitante = servicioUsuario.buscarPorId(solicitud.getIdSolicitante())
                            .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
                    solicitante.getMonedero().devolverCompromiso();
                    solicitante.incrementarVersion();
                    servicioUsuario.guardar(solicitante);
                }
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

            if (esNecesidad && precio > 0) {
                // El demandante (dueño de la necesidad) paga al ofertante
                Usuario demandante = servicioUsuario.buscarPorId(pub.getIdUsuario())
                        .orElseThrow(() -> new IllegalArgumentException("Demandante no encontrado."));
                if (demandante.getMonedero().getSaldoDisponible() < precio) {
                    throw new IllegalStateException("Saldo insuficiente. Necesitas " + precio
                            + " creditos, pero tienes " + demandante.getMonedero().getSaldoDisponible() + " disponibles.");
                }
                demandante.getMonedero().comprometer(precio);
                demandante.incrementarVersion();
                servicioUsuario.guardar(demandante);
            }

            solicitud.aceptar();

            if (esNecesidad) {
                // Roles invertidos: ofertante = quien ofrece el servicio (solicitante)
                // demandante = quien necesita el servicio (dueño publicación)
                Transaccion tx = new Transaccion(
                    pub.getIdPublicacion(),
                    solicitud.getIdSolicitante(),
                    pub.getIdUsuario(),
                    precio
                );
                repositorioTransaccion.guardar(tx);

                servicioNotificacion.enviarNotificacion(pub.getIdUsuario(), solicitud.getIdSolicitante(),
                        "Tu oferta para " + pub.getNombreServicio() + " fue ACEPTADA. Se retuvieron "
                                + precio + " créditos al demandante.",
                        TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);

                Usuario ofertante = servicioUsuario.buscarPorId(solicitud.getIdSolicitante())
                        .orElseThrow(() -> new IllegalArgumentException("Ofertante no encontrado."));
                Usuario demandante = servicioUsuario.buscarPorId(pub.getIdUsuario())
                        .orElseThrow(() -> new IllegalArgumentException("Demandante no encontrado."));
                servicioNotificacion.enviarNotificacion("SISTEMA", pub.getIdUsuario(),
                        "Datos de contacto del ofertante " + ofertante.getNombre()
                                + ": Correo: " + ofertante.getCorreoElectronico()
                                + " — Teléfono: " + ofertante.getTelefono(),
                        TipoNotificacion.TRANSACCION_ACTUALIZADA);
                servicioNotificacion.enviarNotificacion("SISTEMA", solicitud.getIdSolicitante(),
                        "Datos de contacto del demandante " + demandante.getNombre()
                                + ": Correo: " + demandante.getCorreoElectronico()
                                + " — Teléfono: " + demandante.getTelefono(),
                        TipoNotificacion.TRANSACCION_ACTUALIZADA);
            } else {
                Transaccion tx = new Transaccion(
                    pub.getIdPublicacion(),
                    pub.getIdUsuario(),
                    solicitud.getIdSolicitante(),
                    precio
                );
                repositorioTransaccion.guardar(tx);

                servicioNotificacion.enviarNotificacion(pub.getIdUsuario(), solicitud.getIdSolicitante(),
                        "Tu solicitud para " + pub.getNombreServicio() + " fue ACEPTADA. Se retuvieron "
                                + precio + " creditos.",
                        TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);

                Usuario ofertante = servicioUsuario.buscarPorId(pub.getIdUsuario())
                        .orElseThrow(() -> new IllegalArgumentException("Ofertante no encontrado."));
                Usuario demandante = servicioUsuario.buscarPorId(solicitud.getIdSolicitante())
                        .orElseThrow(() -> new IllegalArgumentException("Demandante no encontrado."));
                servicioNotificacion.enviarNotificacion("SISTEMA", solicitud.getIdSolicitante(),
                        "Datos de contacto del ofertante " + ofertante.getNombre()
                                + ": Correo: " + ofertante.getCorreoElectronico()
                                + " — Teléfono: " + ofertante.getTelefono(),
                        TipoNotificacion.TRANSACCION_ACTUALIZADA);
                servicioNotificacion.enviarNotificacion("SISTEMA", pub.getIdUsuario(),
                        "Datos de contacto del demandante " + demandante.getNombre()
                                + ": Correo: " + demandante.getCorreoElectronico()
                                + " — Teléfono: " + demandante.getTelefono(),
                        TipoNotificacion.TRANSACCION_ACTUALIZADA);
            }
        } else {
            if (solicitud.getEstado() == EstadoSolicitud.PENDIENTE) {
                if (pub.getTipoPublicacion().equals("HABILIDAD") && precio > 0) {
                    Usuario solicitante = servicioUsuario.buscarPorId(solicitud.getIdSolicitante())
                            .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
                    solicitante.getMonedero().devolverCompromiso();
                    solicitante.incrementarVersion();
                    servicioUsuario.guardar(solicitante);
                }
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
