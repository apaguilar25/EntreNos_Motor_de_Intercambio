package es.ucab.entrenos.modulos.publicacion.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.modelos.HabilidadOfrecida;
import es.ucab.entrenos.modulos.identidad.modelos.NecesidadRegistrada;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioHabilidad;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.publicacion.dtos.PublicacionResponseDTO;
import es.ucab.entrenos.modulos.publicacion.dto.RecomendacionDTO;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoSolicitud;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoTransaccion;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Solicitud;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioPublicacion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioSolicitud;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import es.ucab.entrenos.modulos.publicacion.utilidades.CachePublicacion;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioPublicacion {
    private final IRepositorioPublicacion repositorioPublicacion;
    private final IRepositorioTransaccion repositorioTransaccion;
    private final ServicioUsuario servicioUsuario;
    private final ServicioNotificacion servicioNotificacion;
    private final ServicioHabilidad servicioHabilidad;

    @org.springframework.beans.factory.annotation.Autowired
    private IRepositorioSolicitud repositorioSolicitud;

    private static final Logger log = LoggerFactory.getLogger(ServicioPublicacion.class);
    private static final Random RANDOM = new Random();

    @org.springframework.beans.factory.annotation.Autowired
    private CachePublicacion cachePublicacion;

    public ServicioPublicacion(IRepositorioPublicacion repositorioPublicacion,
                                IRepositorioTransaccion repositorioTransaccion,
                                ServicioUsuario servicioUsuario,
                                ServicioNotificacion servicioNotificacion,
                                ServicioHabilidad servicioHabilidad) {
        this.repositorioPublicacion = repositorioPublicacion;
        this.repositorioTransaccion = repositorioTransaccion;
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
        this.servicioHabilidad = servicioHabilidad;
    }

    @PostConstruct
    public void initCache() {
        refrescarCache();
    }

    public PublicacionResponseDTO crearPublicacion(Publicacion publicacion) {
        if (publicacion.getIdPublicacion() == null || publicacion.getIdPublicacion().isEmpty()) {
            publicacion.setIdPublicacion("PUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        validarCategoriaPublicacion(publicacion);
        publicacion.setDisponible(true);
        repositorioPublicacion.guardar(publicacion);
        refrescarCache();
        return toResponseDTO(publicacion);
    }

    private void validarCategoriaPublicacion(Publicacion publicacion) {
        if (publicacion.getIdInstanciaCatalogo() != null) {
            Usuario usuario = servicioUsuario.buscarPorId(publicacion.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
            String categoria = obtenerCategoriaDeUsuario(publicacion, usuario);
            if (categoria != null) {
                boolean existeEnCatalogoMaestro = servicioHabilidad.obtenerTodas().stream()
                        .anyMatch(h -> h.getCategoria().equalsIgnoreCase(categoria));
                if (!existeEnCatalogoMaestro) {
                    throw new IllegalArgumentException("La categoría '" + categoria + "' no existe en el catálogo maestro de habilidades.");
                }
            }
        }
    }

    private String obtenerCategoriaDeUsuario(Publicacion publicacion, Usuario usuario) {
        if ("HABILIDAD".equalsIgnoreCase(publicacion.getTipoPublicacion())) {
            return usuario.getHabilidadesOfrecidas().stream()
                    .filter(h -> h.getIdInstancia().equals(publicacion.getIdInstanciaCatalogo()))
                    .findFirst()
                    .map(h -> h.getHabilidadBase().getCategoria())
                    .orElse(null);
        } else if ("NECESIDAD".equalsIgnoreCase(publicacion.getTipoPublicacion())) {
            return usuario.getNecesidadesRegistradas().stream()
                    .filter(n -> n.getIdInstancia().equals(publicacion.getIdInstanciaCatalogo()))
                    .findFirst()
                    .map(n -> n.getNecesidadBase().getCategoria())
                    .orElse(null);
        }
        return null;
    }

    public Publicacion actualizarPublicacion(String idPublicacion, int precioCreditos, String descripcion) {
        Publicacion pub = repositorioPublicacion.obtenerPorId(idPublicacion)
                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada en el muro: " + idPublicacion));

        pub.setPrecioCreditos(precioCreditos);
        pub.setDescripcion(descripcion);
        repositorioPublicacion.guardar(pub);
        refrescarCache();
        return pub;
    }


    private void refrescarCache() {
        List<PublicacionResponseDTO> todas = repositorioPublicacion.obtenerTodas().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        cachePublicacion.refrescar(todas);
        log.info("CachePublicacion inicializada con {} publicaciones en total", todas.size());
    }

    public List<Publicacion> obtenerTodasLasPublicaciones() {
        return repositorioPublicacion.obtenerTodas();
    }

    public List<PublicacionResponseDTO> obtenerPublicacionesFiltradas(String tipo, String servicio) {
        if (tipo == null && servicio == null) {
            long inicio = System.currentTimeMillis();
            List<PublicacionResponseDTO> resultado = cachePublicacion.getTop10();
            log.info("CON CACHÉ: {} ms para obtener top 10 por reputación", System.currentTimeMillis() - inicio);
            return resultado;
        }
        return repositorioPublicacion.obtenerTodas().stream()
                .filter(p -> tipo == null || tipo.isEmpty() || p.getTipoPublicacion().equalsIgnoreCase(tipo))
                .filter(p -> servicio == null || servicio.isEmpty() ||
                        p.getNombreServicio().toLowerCase().contains(servicio.toLowerCase()) ||
                        p.getDescripcion().toLowerCase().contains(servicio.toLowerCase()))
                .map(this::toResponseDTO)
                .sorted(Comparator.comparingDouble(PublicacionResponseDTO::getReputacionUsuario).reversed())
                .collect(Collectors.toList());
    }

    public List<PublicacionResponseDTO> obtenerPublicacionesSinCache() {
        long inicio = System.currentTimeMillis();
        List<PublicacionResponseDTO> resultado = repositorioPublicacion.obtenerTodas().stream()
                .map(this::toResponseDTO)
                .sorted(Comparator.comparingDouble(PublicacionResponseDTO::getReputacionUsuario).reversed())
                .limit(10)
                .collect(Collectors.toList());
        log.info("Publicaciones leidas desde disco ({} ms)", System.currentTimeMillis() - inicio);
        return resultado;
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
        refrescarCache();
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
            refrescarCache();
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
            refrescarCache();
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

        String categoria = obtenerCategoriaDesdeCatalogo(pub);
        Habilidad habilidadBase = new Habilidad(UUID.randomUUID().toString(), categoria);
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

    private String obtenerCategoriaDesdeCatalogo(Publicacion pub) {
        if (pub.getIdInstanciaCatalogo() != null) {
            Usuario propietario = servicioUsuario.buscarPorId(pub.getIdUsuario()).orElse(null);
            if (propietario != null) {
                String categoria = propietario.getHabilidadesOfrecidas().stream()
                        .filter(h -> h.getIdInstancia().equals(pub.getIdInstanciaCatalogo()))
                        .map(h -> h.getHabilidadBase().getCategoria())
                        .findFirst().orElse(null);
                if (categoria == null) {
                    categoria = propietario.getNecesidadesRegistradas().stream()
                            .filter(n -> n.getIdInstancia().equals(pub.getIdInstanciaCatalogo()))
                            .map(n -> n.getNecesidadBase().getCategoria())
                            .findFirst().orElse(null);
                }
                if (categoria != null) {
                    return categoria;
                }
            }
        }
        throw new IllegalArgumentException("No se pudo determinar la categoría del catálogo para la publicación: " + pub.getNombreServicio());
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
            dto.setReputacionUsuario(Math.round(u.getPromedioCalificacion() * 10.0) / 10.0);
            dto.setEsVecinoDestacado(u.isEsVecinoDestacado());
        });
        return dto;
    }
}
