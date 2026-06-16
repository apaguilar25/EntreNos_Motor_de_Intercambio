package es.ucab.entrenos.modulos.publicacion.servicios;

import es.ucab.entrenos.modulos.gamificacion.modelos.LogroDesbloqueado;
import es.ucab.entrenos.modulos.gamificacion.servicios.ServicioGamificacion;
import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.modelos.HabilidadOfrecida;
import es.ucab.entrenos.modulos.identidad.modelos.NecesidadRegistrada;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.publicacion.dtos.ConfirmacionTransaccionResponseDTO;
import es.ucab.entrenos.modulos.publicacion.dto.RecomendacionDTO;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoTransaccion;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioPublicacion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioPublicacion {
    private final IRepositorioPublicacion repositorioPublicacion;
    private final IRepositorioTransaccion repositorioTransaccion;
    private final ServicioUsuario servicioUsuario;
    private final ServicioNotificacion servicioNotificacion;
    private final ServicioGamificacion servicioGamificacion;
    private static final Random RANDOM = new Random();
    private static final String[] COMENTARIOS_SIMULADOS = {
        "Excelente servicio, muy profesional.",
        "Buen trabajo, quedé satisfecho.",
        "Cumplió con lo acordado, sin problemas.",
        "El servicio fue regular, esperaba más.",
        "No cumplió con lo prometido."
    };

    public ServicioPublicacion(IRepositorioPublicacion repositorioPublicacion,
                               IRepositorioTransaccion repositorioTransaccion,
                               ServicioUsuario servicioUsuario,
                               ServicioNotificacion servicioNotificacion,
                               ServicioGamificacion servicioGamificacion) {
        this.repositorioPublicacion = repositorioPublicacion;
        this.repositorioTransaccion = repositorioTransaccion;
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
        this.servicioGamificacion = servicioGamificacion;
    }

    public List<Publicacion> obtenerTodasLasPublicaciones() {
        return repositorioPublicacion.obtenerTodas();
    }

    public List<Publicacion> obtenerPublicacionesFiltradas(String tipo, String servicio) {
        return repositorioPublicacion.obtenerTodas().stream()
                .filter(p -> tipo == null || tipo.isEmpty() || p.getTipoPublicacion().equalsIgnoreCase(tipo))
                .filter(p -> servicio == null || servicio.isEmpty() ||
                        p.getNombreServicio().toLowerCase().contains(servicio.toLowerCase()) ||
                        p.getDescripcion().toLowerCase().contains(servicio.toLowerCase()))
                .sorted(Comparator.comparingDouble(Publicacion::getReputacionUsuario).reversed())
                .collect(Collectors.toList());
    }

    public List<RecomendacionDTO> obtenerRecomendadas(String idUsuario) {
        Usuario usuario = servicioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));

        Set<String> habilidades = categoriasDe(usuario.getHabilidadesOfrecidas().stream()
                .map(h -> h.getHabilidadBase().getCategoria()).collect(Collectors.toList()));
        Set<String> necesidades = categoriasDe(usuario.getNecesidadesRegistradas().stream()
                .map(n -> n.getNecesidadBase().getCategoria()).collect(Collectors.toList()));
        Set<String> catalogoCompleto = union(habilidades, necesidades);

        List<RecomendacionDTO> recomendadas = repositorioPublicacion.obtenerTodas().stream()
                .filter(p -> !p.getIdUsuario().equals(idUsuario))
                .filter(p -> matchDireccional(p, habilidades, necesidades))
                .map(p -> new RecomendacionDTO(p,
                        catalogoCompleto.contains(nombreServicioNormalizado(p)),
                        tipoCoincidencia(p, habilidades, necesidades)))
                .collect(Collectors.toList());

        recomendadas.sort(porReputacionConEstrellas());
        return recomendadas;
    }

    private Set<String> categoriasDe(List<String> lista) {
        return lista.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    private Set<String> union(Set<String> a, Set<String> b) {
        Set<String> resultado = new HashSet<>(a);
        resultado.addAll(b);
        return resultado;
    }

    private String nombreServicioNormalizado(Publicacion p) {
        return p.getNombreServicio().toLowerCase();
    }

    private boolean matchDireccional(Publicacion p, Set<String> habilidades, Set<String> necesidades) {
        String nombre = nombreServicioNormalizado(p);
        if ("HABILIDAD".equalsIgnoreCase(p.getTipoPublicacion())) {
            return necesidades.contains(nombre);
        }
        if ("NECESIDAD".equalsIgnoreCase(p.getTipoPublicacion())) {
            return habilidades.contains(nombre);
        }
        return false;
    }

    private String tipoCoincidencia(Publicacion p, Set<String> habilidades, Set<String> necesidades) {
        String nombre = nombreServicioNormalizado(p);
        if ("HABILIDAD".equalsIgnoreCase(p.getTipoPublicacion()) && necesidades.contains(nombre)) {
            return "OFERTA_CUBRE_MI_NECESIDAD";
        }
        if ("NECESIDAD".equalsIgnoreCase(p.getTipoPublicacion()) && habilidades.contains(nombre)) {
            return "DEMANDA_COINCIDE_CON_MI_HABILIDAD";
        }
        return "SIN_COINCIDENCIA_DIRECCIONAL";
    }

    private Comparator<RecomendacionDTO> porReputacionConEstrellas() {
        return (a, b) -> {
            double repA = a.getPublicacion().getReputacionUsuario();
            double repB = b.getPublicacion().getReputacionUsuario();
            boolean estrellaA = repA >= 4.0;
            boolean estrellaB = repB >= 4.0;
            if (estrellaA != estrellaB) {
                return estrellaA ? -1 : 1;
            }
            return Double.compare(repB, repA);
        };
    }

    public Publicacion crearPublicacion(Publicacion publicacion) {
        if (publicacion.getIdPublicacion() == null || publicacion.getIdPublicacion().isEmpty()) {
            publicacion.setIdPublicacion("PUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        publicacion.setDisponible(true);
        publicacion.setFechaCreacion(System.currentTimeMillis());
        repositorioPublicacion.guardar(publicacion);
        return publicacion;
    }

    public Publicacion actualizarPublicacion(String idPublicacion, int precioCreditos, String descripcion) {
        Publicacion pub = repositorioPublicacion.obtenerPorId(idPublicacion)
                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada en el muro: " + idPublicacion));
        
        pub.setPrecioCreditos(precioCreditos);
        pub.setDescripcion(descripcion);
        repositorioPublicacion.guardar(pub);
        return pub;
    }

    public Optional<Publicacion> obtenerPublicacionPorId(String id) {
        return repositorioPublicacion.obtenerPorId(id);
    }

    public boolean eliminarPublicacion(String id) {
        List<Publicacion> todas = repositorioPublicacion.obtenerTodas();
        boolean removido = todas.removeIf(p -> p.getIdPublicacion().equalsIgnoreCase(id));
        if (removido) {
            repositorioPublicacion.guardarTodas(todas);
        }
        return removido;
    }

    public Publicacion solicitarPublicacion(String idPublicacion, String idSolicitante, String nombreSolicitante) {
        Publicacion pub = repositorioPublicacion.obtenerPorId(idPublicacion)
                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada: " + idPublicacion));
        if (pub.haExpiradoPlazoRespuesta()) {
            pub.setEstadoSolicitud(Publicacion.ESTADO_SOLICITUD_EXPIRADA);
            repositorioPublicacion.guardar(pub);
            servicioNotificacion.enviarNotificacion("SISTEMA", pub.getIdUsuario(),
                    "La solicitud de " + nombreSolicitante + " para " + pub.getNombreServicio()
                            + " ha expirado automáticamente.", TipoNotificacion.ALERTA_SISTEMA);
            servicioNotificacion.enviarNotificacion("SISTEMA", idSolicitante,
                    "Tu solicitud para " + pub.getNombreServicio() + " ha expirado (sin respuesta del dueño).",
                    TipoNotificacion.ALERTA_SISTEMA);
            throw new IllegalStateException("El plazo de respuesta ha expirado.");
        }
        if (pub.getTipoPublicacion().equals("HABILIDAD") && pub.getPrecioCreditos() > 0) {
            Usuario solicitante = servicioUsuario.buscarPorId(idSolicitante)
                    .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
            if (solicitante.getMonedero().getSaldoDisponible() < pub.getPrecioCreditos()) {
                throw new IllegalStateException("Saldo insuficiente. Necesitas " + pub.getPrecioCreditos()
                        + " créditos, pero tienes " + solicitante.getMonedero().getSaldoDisponible() + " disponibles.");
            }
        }
        pub.solicitar(idSolicitante, nombreSolicitante);
        repositorioPublicacion.guardar(pub);
        servicioNotificacion.enviarNotificacion(idSolicitante, pub.getIdUsuario(),
                nombreSolicitante + " quiere contratar tu servicio: " + pub.getNombreServicio(),
                TipoNotificacion.NUEVA_SOLICITUD_ENTRANTE, pub.getIdPublicacion(), idSolicitante);
        return pub;
    }

    public Publicacion responderSolicitud(String idPublicacion, String idUsuario, boolean aceptar) {
        Publicacion pub = repositorioPublicacion.obtenerPorId(idPublicacion)
                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada: " + idPublicacion));
        if (!pub.getIdUsuario().equals(idUsuario)) {
            throw new IllegalArgumentException("Solo el dueño de la publicación puede responder la solicitud.");
        }
        String solicitanteId = pub.getIdSolicitante();
        String solicitanteNombre = pub.getNombreSolicitante();
        pub.responderSolicitud(aceptar);
        if (aceptar) {
            Usuario demandante = servicioUsuario.buscarPorId(solicitanteId)
                    .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
            demandante.getMonedero().retener(pub.getPrecioCreditos());
            demandante.incrementarVersion();
            servicioUsuario.guardar(demandante);
            Transaccion tx = new Transaccion(
                pub.getIdPublicacion(),
                pub.getIdUsuario(),
                pub.getIdSolicitante(),
                pub.getNombreServicio(),
                pub.getDescripcion(),
                pub.getPrecioCreditos()
            );
            repositorioTransaccion.guardar(tx);
            servicioNotificacion.enviarNotificacion(pub.getIdUsuario(), solicitanteId,
                    "Tu solicitud para " + pub.getNombreServicio() + " fue ACEPTADA. Se retuvieron "
                            + pub.getPrecioCreditos() + " créditos.",
                    TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO, pub.getIdPublicacion(), null);
        } else {
            servicioNotificacion.enviarNotificacion(pub.getIdUsuario(), solicitanteId,
                    "Tu solicitud para " + pub.getNombreServicio() + " fue RECHAZADA.",
                    TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO, pub.getIdPublicacion(), null);
        }
        repositorioPublicacion.guardar(pub);
        return pub;
    }

    public List<Transaccion> obtenerTodasLasTransacciones() {
        return repositorioTransaccion.obtenerTodas();
    }

    public Optional<Transaccion> obtenerTransaccionPorId(String id) {
        return repositorioTransaccion.obtenerPorId(id);
    }

    public Transaccion crearTransaccion(Transaccion transaccion) {
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
        EstadoTransaccion estadoAnterior = t.getEstado();
        t.confirmarEntregaOfertante();
        if (t.getEstado() == EstadoTransaccion.INICIADA || t.getEstado() == EstadoTransaccion.FINALIZADA) {
            simularDemandante(t);
        }
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
                "El ofertante confirmó la entrega del servicio: " + t.getNombreServicio(),
                TipoNotificacion.TRANSACCION_ACTUALIZADA);
        return new ConfirmacionTransaccionResponseDTO(t, nuevosLogros);
    }

    public ConfirmacionTransaccionResponseDTO confirmarDemandante(String idTransaccion) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        EstadoTransaccion estadoAnterior = t.getEstado();
        t.confirmarRecepcionDemandante();
        if (t.getEstado() == EstadoTransaccion.INICIADA || t.getEstado() == EstadoTransaccion.FINALIZADA) {
            simularOfertante(t);
        }
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
                "El demandante confirmó la recepción del servicio: " + t.getNombreServicio(),
                TipoNotificacion.TRANSACCION_ACTUALIZADA);
        return new ConfirmacionTransaccionResponseDTO(t, nuevosLogros);
    }

    private void procesarPago(Transaccion t) {
        Usuario ofertante = servicioUsuario.buscarPorId(t.getIdOfertante())
                .orElseThrow(() -> new IllegalArgumentException("Ofertante no encontrado."));
        Usuario demandante = servicioUsuario.buscarPorId(t.getIdDemandante())
                .orElseThrow(() -> new IllegalArgumentException("Demandante no encontrado."));
        demandante.getMonedero().liberarRetencion();
        demandante.incrementarVersion();
        ofertante.getMonedero().acreditar(t.getCreditosRetenidos());
        ofertante.incrementarVersion();
        servicioUsuario.guardar(demandante);
        servicioUsuario.guardar(ofertante);
        servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdOfertante(),
                "Recibiste " + t.getCreditosRetenidos() + " créditos por el servicio: " + t.getNombreServicio(),
                TipoNotificacion.MOVIMIENTO_MONEDERO);
        servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdDemandante(),
                "Se liberaron " + t.getCreditosRetenidos() + " créditos retenidos por: " + t.getNombreServicio(),
                TipoNotificacion.MOVIMIENTO_MONEDERO);
    }

    private void simularDemandante(Transaccion t) {
        if (t.isConfirmacionDemandante()) return;
        boolean confirma = RANDOM.nextBoolean();
        if (confirma) {
            t.setConfirmacionDemandante(true);
            t.setFechaConfirmacionDemandante(System.currentTimeMillis());
            t.setCalificacionOfertante(RANDOM.nextInt(5) + 1);
            t.setComentarioOfertante(COMENTARIOS_SIMULADOS[RANDOM.nextInt(COMENTARIOS_SIMULADOS.length)]);
        } else {
            t.setSancionado(true);
        }
        t.confirmarRecepcionDemandante();
    }

    private void simularOfertante(Transaccion t) {
        if (t.isConfirmacionOfertante()) return;
        boolean confirma = RANDOM.nextBoolean();
        if (confirma) {
            t.setConfirmacionOfertante(true);
            t.setFechaConfirmacionOfertante(System.currentTimeMillis());
        } else {
            t.setSancionado(true);
        }
        t.confirmarEntregaOfertante();
    }

    public Transaccion calificar(String idTransaccion, String idUsuario, int calificacion, String comentario) {
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
        if (t.getCalificacionOfertante() != null) {
            throw new IllegalStateException("Ya calificaste esta transacción.");
        }
        t.setCalificacionOfertante(calificacion);
        t.setComentarioOfertante(comentario);
        repositorioTransaccion.guardar(t);
        servicioUsuario.actualizarReputacion(t.getIdOfertante(), calificacion);
        actualizarReputacionEnPublicaciones(t.getIdOfertante());
        return t;
    }

    private void actualizarReputacionEnPublicaciones(String idUsuario) {
        Usuario usuario = servicioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));
        double nuevaReputacion = usuario.getPromedioCalificacion();
        List<Publicacion> todas = repositorioPublicacion.obtenerTodas();
        for (int i = 0; i < todas.size(); i++) {
            Publicacion p = todas.get(i);
            if (p.getIdUsuario().equals(idUsuario)) {
                p.setReputacionUsuario(nuevaReputacion);
            }
        }
        repositorioPublicacion.guardarTodas(todas);
    }

    public List<Transaccion> obtenerCalificacionesPorPublicacion(String idPublicacion) {
        return repositorioTransaccion.obtenerTodas().stream()
                .filter(t -> t.getIdPublicacion().equals(idPublicacion)
                        && t.getEstado() == EstadoTransaccion.FINALIZADA)
                .collect(Collectors.toList());
    }

    public List<RecomendacionDTO> adoptarRecomendacion(String idUsuario, String idPublicacion) {
        Publicacion pub = repositorioPublicacion.obtenerPorId(idPublicacion)
                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada: " + idPublicacion));
        if (pub.getIdUsuario().equals(idUsuario)) {
            throw new IllegalArgumentException("No puedes adoptar tu propia publicación.");
        }
        Usuario usuario = servicioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));
        Habilidad habilidadBase = new Habilidad(UUID.randomUUID().toString(), pub.getNombreServicio());
        if ("HABILIDAD".equalsIgnoreCase(pub.getTipoPublicacion())) {
            NecesidadRegistrada necesidad = new NecesidadRegistrada(habilidadBase,
                    "Estoy interesado en: " + pub.getNombreServicio() + " - " + pub.getDescripcion());
            usuario.agregarNecesidad(necesidad);
        } else if ("NECESIDAD".equalsIgnoreCase(pub.getTipoPublicacion())) {
            HabilidadOfrecida habilidad = new HabilidadOfrecida(habilidadBase,
                    pub.getPrecioCreditos() > 0 ? pub.getPrecioCreditos() : 10,
                    "Ofrezco: " + pub.getNombreServicio() + " - " + pub.getDescripcion());
            usuario.agregarHabilidadOfrecida(habilidad);
        } else {
            throw new IllegalArgumentException("Tipo de publicación no soportado: " + pub.getTipoPublicacion());
        }
        usuario.incrementarVersion();
        servicioUsuario.guardar(usuario);
        return obtenerRecomendadas(idUsuario);
    }

    public Transaccion reportarIncidencia(String idTransaccion, String descripcion, String urlEvidencia) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        t.reportarIncidencia(descripcion, urlEvidencia);
        repositorioTransaccion.guardar(t);
        servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdOfertante(),
                "Se ha reportado una incidencia en la transacción \"" + t.getNombreServicio()
                        + "\". El intercambio ha sido marcado bajo revisión. Los créditos permanecen bloqueados.",
                TipoNotificacion.ALERTA_SISTEMA);
        servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdDemandante(),
                "Se ha reportado una incidencia en la transacción \"" + t.getNombreServicio()
                        + "\". El intercambio ha sido marcado bajo revisión. Los créditos permanecen bloqueados.",
                TipoNotificacion.ALERTA_SISTEMA);
        return t;
    }

    public Transaccion cancelarTransaccion(String idTransaccion, String idUsuario, String motivo) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        if (t.getEstado() == EstadoTransaccion.FINALIZADA || t.getEstado() == EstadoTransaccion.EN_DISPUTA) {
            throw new IllegalStateException("No se puede cancelar una transacción en estado " + t.getEstado() + ".");
        }
        if (motivo == null || motivo.trim().length() < 10) {
            throw new IllegalArgumentException("Debe proporcionar un motivo de al menos 10 caracteres.");
        }
        if (t.getEstado() == EstadoTransaccion.INICIADA) {
            Usuario demandante = servicioUsuario.buscarPorId(t.getIdDemandante())
                    .orElseThrow(() -> new IllegalArgumentException("Demandante no encontrado."));
            demandante.getMonedero().liberarRetencion();
            demandante.incrementarVersion();
            servicioUsuario.guardar(demandante);
        }
        t.setEstado(EstadoTransaccion.RECHAZADA);
        repositorioTransaccion.guardar(t);
        String contraparte = t.getIdOfertante().equals(idUsuario) ? t.getIdDemandante() : t.getIdOfertante();
        servicioNotificacion.enviarNotificacion(idUsuario, contraparte,
                "La transacción \"" + t.getNombreServicio() + "\" ha sido cancelada. Motivo: " + motivo
                        + ". Si consideras que es injusto, puedes reportar una incidencia desde la sección de transacciones.",
                TipoNotificacion.ALERTA_SISTEMA);
        return t;
    }
}
