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
import es.ucab.entrenos.modulos.reputacion.servicios.ServicioReputacion;
import es.ucab.entrenos.modulos.publicacion.dtos.PublicacionResponseDTO;
import es.ucab.entrenos.modulos.publicacion.dto.RecomendacionDTO;
import es.ucab.entrenos.modulos.publicacion.modelos.Cancelacion;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoCancelacion;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoSolicitud;
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
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioSolicitud;
import es.ucab.entrenos.modulos.publicacion.modelos.Solicitud;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioPublicacion {
    private final IRepositorioPublicacion repositorioPublicacion;
    private final IRepositorioTransaccion repositorioTransaccion;
    private final IRepositorioIncidencia repositorioIncidencia;
    private final IRepositorioCancelacion repositorioCancelacion;
    private final ServicioUsuario servicioUsuario;
    private final ServicioNotificacion servicioNotificacion;
    private final ServicioGamificacion servicioGamificacion;
    private final ServicioReputacion servicioReputacion;
    
    @org.springframework.beans.factory.annotation.Autowired
    private IRepositorioSolicitud repositorioSolicitud;

    private static final Random RANDOM = new Random();
    public ServicioPublicacion(IRepositorioPublicacion repositorioPublicacion,
                               IRepositorioTransaccion repositorioTransaccion,
                               IRepositorioIncidencia repositorioIncidencia,
                               IRepositorioCancelacion repositorioCancelacion,
                               ServicioUsuario servicioUsuario,
                               ServicioNotificacion servicioNotificacion,
                               ServicioGamificacion servicioGamificacion,
                               ServicioReputacion servicioReputacion) {
        this.repositorioPublicacion = repositorioPublicacion;
        this.repositorioTransaccion = repositorioTransaccion;
        this.repositorioIncidencia = repositorioIncidencia;
        this.repositorioCancelacion = repositorioCancelacion;
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
        this.servicioGamificacion = servicioGamificacion;
        this.servicioReputacion = servicioReputacion;
    }

    public List<Publicacion> obtenerTodasLasPublicaciones() {
        return repositorioPublicacion.obtenerTodas();
    }

    public List<PublicacionResponseDTO> obtenerPublicacionesFiltradas(String tipo, String servicio) {
        return repositorioPublicacion.obtenerTodas().stream()
                .filter(p -> tipo == null || tipo.isEmpty() || p.getTipoPublicacion().equalsIgnoreCase(tipo))
                .filter(p -> servicio == null || servicio.isEmpty() ||
                        p.getNombreServicio().toLowerCase().contains(servicio.toLowerCase()) ||
                        p.getDescripcion().toLowerCase().contains(servicio.toLowerCase()))
                .map(this::toResponseDTO)
                .sorted(Comparator.comparingDouble(PublicacionResponseDTO::getReputacionUsuario).reversed())
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

        List<Publicacion> todasPubs = repositorioPublicacion.obtenerTodas();
        List<RecomendacionDTO> recomendadas = todasPubs.stream()
                .filter(p -> !p.getIdUsuario().equals(idUsuario))
                .filter(p -> matchDireccional(p, habilidades, necesidades))
                .map(p -> new RecomendacionDTO(toResponseDTO(p),
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

    public PublicacionResponseDTO crearPublicacion(Publicacion publicacion) {
        if (publicacion.getIdPublicacion() == null || publicacion.getIdPublicacion().isEmpty()) {
            publicacion.setIdPublicacion("PUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        publicacion.setDisponible(true);
        repositorioPublicacion.guardar(publicacion);
        return toResponseDTO(publicacion);
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

    public Optional<Publicacion> obtenerPublicacionPorInstanciaCatalogo(String idInstanciaCatalogo) {
        return repositorioPublicacion.obtenerTodas().stream()
                .filter(p -> idInstanciaCatalogo.equals(p.getIdInstanciaCatalogo()))
                .findFirst();
    }

    public void guardarPublicacion(Publicacion publicacion) {
        repositorioPublicacion.guardar(publicacion);
    }

    public List<PublicacionResponseDTO> obtenerPublicacionesPorUsuario(String idUsuario) {
        return repositorioPublicacion.obtenerTodas().stream()
                .filter(p -> p.getIdUsuario().equals(idUsuario))
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public boolean eliminarPublicacion(String id) {
        cancelarVinculosPublicacion(id);
        List<Publicacion> todas = repositorioPublicacion.obtenerTodas();
        boolean removido = todas.removeIf(p -> p.getIdPublicacion().equalsIgnoreCase(id));
        if (removido) {
            repositorioPublicacion.guardarTodas(todas);
        }
        return removido;
    }

    public boolean eliminarPublicacionPorInstancia(String idInstanciaCatalogo) {
        Optional<Publicacion> pubOpt = obtenerPublicacionPorInstanciaCatalogo(idInstanciaCatalogo);
        if (pubOpt.isPresent()) {
            cancelarVinculosPublicacion(pubOpt.get().getIdPublicacion());
        }
        List<Publicacion> todas = repositorioPublicacion.obtenerTodas();
        boolean removido = todas.removeIf(p -> idInstanciaCatalogo.equals(p.getIdInstanciaCatalogo()));
        if (removido) {
            repositorioPublicacion.guardarTodas(todas);
        }
        return removido;
    }

    private void cancelarVinculosPublicacion(String idPublicacion) {
        List<Transaccion> txs = repositorioTransaccion.obtenerTodas().stream()
            .filter(t -> t.getIdPublicacion().equalsIgnoreCase(idPublicacion) && 
                (t.getEstado() == EstadoTransaccion.PENDIENTE || t.getEstado() == EstadoTransaccion.INICIADA || t.getEstado() == EstadoTransaccion.EN_DISPUTA))
            .collect(Collectors.toList());

        for (Transaccion t : txs) {
            t.setEstado(EstadoTransaccion.RECHAZADA);
            Usuario demandante = servicioUsuario.buscarPorId(t.getIdDemandante()).orElse(null);
            if (demandante != null && t.getCreditosRetenidos() > 0) {
                demandante.getMonedero().devolverCompromiso();
                demandante.incrementarVersion();
                servicioUsuario.guardar(demandante);
            }
            repositorioTransaccion.guardar(t);
            servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdDemandante(), "La publicación (" + idPublicacion + ") ha sido eliminada por su dueño. Transacción cancelada y créditos liberados.", TipoNotificacion.ALERTA_SISTEMA);
            servicioNotificacion.enviarNotificacion("SISTEMA", t.getIdOfertante(), "Has eliminado la publicación (" + idPublicacion + "). Transacción cancelada.", TipoNotificacion.ALERTA_SISTEMA);
        }

        if (repositorioSolicitud != null) {
            List<Solicitud> sols = repositorioSolicitud.obtenerTodas().stream()
                .filter(s -> s.getIdPublicacion().equalsIgnoreCase(idPublicacion) && s.getEstado() == EstadoSolicitud.PENDIENTE)
                .collect(Collectors.toList());
            for (Solicitud s : sols) {
                s.setEstado(EstadoSolicitud.RECHAZADA);
                Usuario solicitante = servicioUsuario.buscarPorId(s.getIdSolicitante()).orElse(null);
                Publicacion pub = obtenerPublicacionPorId(idPublicacion).orElse(null);
                if (solicitante != null && pub != null && pub.getTipoPublicacion().equals("HABILIDAD") && pub.getPrecioCreditos() > 0) {
                    solicitante.getMonedero().devolverCompromiso();
                    solicitante.incrementarVersion();
                    servicioUsuario.guardar(solicitante);
                }
                repositorioSolicitud.guardar(s);
                servicioNotificacion.enviarNotificacion("SISTEMA", s.getIdSolicitante(), "La publicación (" + idPublicacion + ") que solicitaste fue eliminada. Solicitud cancelada y créditos liberados.", TipoNotificacion.ALERTA_SISTEMA);
            }
        }
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
        Publicacion pub = obtenerPublicacionPorId(t.getIdPublicacion())
                .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada."));
        EstadoTransaccion estadoAnteior = t.getEstado();
        t.confirmarEntregaOfertante();
        if (t.getEstado() == EstadoTransaccion.FINALIZADA && estadoAnteior != EstadoTransaccion.FINALIZADA) {
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
        Publicacion pub = obtenerPublicacionPorId(t.getIdPublicacion())
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
        Publicacion pub = obtenerPublicacionPorId(t.getIdPublicacion())
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

    public Incidencia reportarIncidencia(String idTransaccion, String idUsuario, String descripcion, String urlEvidencia) {
        Transaccion t = repositorioTransaccion.obtenerPorId(idTransaccion)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + idTransaccion));
        if (descripcion == null || descripcion.trim().length() < 20) {
            throw new IllegalArgumentException("La descripción del incidente debe tener al menos 20 caracteres.");
        }
        Incidencia incidencia = new Incidencia(idTransaccion, idUsuario, descripcion, urlEvidencia);
        repositorioIncidencia.guardar(incidencia);
        t.asignarIncidencia(incidencia.getIdIncidencia());
        repositorioTransaccion.guardar(t);
        Publicacion pub = obtenerPublicacionPorId(t.getIdPublicacion())
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
    }

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
        Publicacion pub = obtenerPublicacionPorId(t.getIdPublicacion())
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
            Publicacion pub = obtenerPublicacionPorId(t.getIdPublicacion()).orElse(null);
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
        Publicacion pub = obtenerPublicacionPorId(t.getIdPublicacion()).orElse(null);
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

        Publicacion pub = obtenerPublicacionPorId(t.getIdPublicacion()).orElse(null);
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

    public PublicacionResponseDTO toResponseDTO(Publicacion p) {
        PublicacionResponseDTO dto = new PublicacionResponseDTO();
        dto.setIdPublicacion(p.getIdPublicacion());
        dto.setIdUsuario(p.getIdUsuario());
        dto.setTipoPublicacion(p.getTipoPublicacion());
        dto.setNombreServicio(p.getNombreServicio());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecioCreditos(p.getPrecioCreditos());
        dto.setDisponible(p.isDisponible());
        dto.setIdInstanciaCatalogo(p.getIdInstanciaCatalogo());
        dto.setVersion(p.getVersion());
        servicioUsuario.buscarPorId(p.getIdUsuario()).ifPresent(u -> {
            dto.setNombreUsuario(u.getNombre());
            dto.setReputacionUsuario(u.getPromedioCalificacion());
            dto.setEsVecinoDestacado(u.isEsVecinoDestacado());
        });
        return dto;
    }
}
