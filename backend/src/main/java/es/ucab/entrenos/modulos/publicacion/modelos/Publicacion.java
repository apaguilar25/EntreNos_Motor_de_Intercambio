package es.ucab.entrenos.modulos.publicacion.modelos;

public class Publicacion {

    private String idPublicacion;
    private String idUsuario;
    private String nombreUsuario;
    private double reputacionUsuario;
    private String tipoPublicacion;
    private String nombreServicio;
    private String descripcion;
    private int precioCreditos;
    private boolean disponible;
    private long fechaCreacion;
    private boolean esVecinoDestacado;

    private String idSolicitante;
    private String nombreSolicitante;
    private String estadoSolicitud;
    private long fechaLimiteRespuesta;
    private static final long PLAZO_RESPUESTA_MS = 5L * 24 * 60 * 60 * 1000;

    private int version;

    public static final String ESTADO_SOLICITUD_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_SOLICITUD_ACEPTADA = "ACEPTADA";
    public static final String ESTADO_SOLICITUD_RECHAZADA = "RECHAZADA";
    public static final String ESTADO_SOLICITUD_EXPIRADA = "EXPIRADA";

    public Publicacion() {}

    public Publicacion(String idUsuario, String nombreUsuario, double reputacionUsuario,
                       String tipoPublicacion, String nombreServicio, String descripcion, int precioCreditos) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.reputacionUsuario = reputacionUsuario;
        this.tipoPublicacion = tipoPublicacion;
        this.nombreServicio = nombreServicio;
        this.descripcion = descripcion;
        this.precioCreditos = precioCreditos;
        this.disponible = true;
        this.fechaCreacion = System.currentTimeMillis();
        this.esVecinoDestacado = false;
    }

    public void solicitar(String idSolicitante, String nombreSolicitante) {
        if (this.idUsuario.equals(idSolicitante)) {
            throw new IllegalArgumentException("No puedes solicitar tu propia publicación.");
        }
        if (!this.disponible) {
            throw new IllegalStateException("La publicación no está disponible.");
        }
        if (this.estadoSolicitud != null) {
            throw new IllegalStateException("Ya existe una solicitud pendiente para esta publicación.");
        }
        this.idSolicitante = idSolicitante;
        this.nombreSolicitante = nombreSolicitante;
        this.estadoSolicitud = ESTADO_SOLICITUD_PENDIENTE;
        this.fechaLimiteRespuesta = System.currentTimeMillis() + PLAZO_RESPUESTA_MS;
    }

    public void responderSolicitud(boolean aceptar) {
        if (haExpiradoPlazoRespuesta()) {
            this.estadoSolicitud = ESTADO_SOLICITUD_EXPIRADA;
            this.idSolicitante = null;
            this.nombreSolicitante = null;
            throw new IllegalStateException("El plazo de 5 días para responder ha expirado.");
        }
        if (!ESTADO_SOLICITUD_PENDIENTE.equals(this.estadoSolicitud)) {
            throw new IllegalStateException("No hay una solicitud pendiente.");
        }
        this.estadoSolicitud = aceptar ? ESTADO_SOLICITUD_ACEPTADA : ESTADO_SOLICITUD_RECHAZADA;
        if (!aceptar) {
            this.idSolicitante = null;
            this.nombreSolicitante = null;
        }
    }

    public boolean haExpiradoPlazoRespuesta() {
        return ESTADO_SOLICITUD_PENDIENTE.equals(this.estadoSolicitud)
                && System.currentTimeMillis() > this.fechaLimiteRespuesta;
    }

    public void limpiarSolicitud() {
        this.idSolicitante = null;
        this.nombreSolicitante = null;
        this.estadoSolicitud = null;
        this.fechaLimiteRespuesta = 0;
    }

    public String getIdPublicacion() { return idPublicacion; }
    public String getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public double getReputacionUsuario() { return reputacionUsuario; }
    public String getTipoPublicacion() { return tipoPublicacion; }
    public String getNombreServicio() { return nombreServicio; }
    public String getDescripcion() { return descripcion; }
    public int getPrecioCreditos() { return precioCreditos; }
    public boolean isDisponible() { return disponible; }
    public long getFechaCreacion() { return fechaCreacion; }
    public boolean isEsVecinoDestacado() { return esVecinoDestacado; }
    public String getIdSolicitante() { return idSolicitante; }
    public String getNombreSolicitante() { return nombreSolicitante; }
    public String getEstadoSolicitud() { return estadoSolicitud; }
    public long getFechaLimiteRespuesta() { return fechaLimiteRespuesta; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public void setIdPublicacion(String idPublicacion) { this.idPublicacion = idPublicacion; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public void setReputacionUsuario(double reputacionUsuario) { this.reputacionUsuario = reputacionUsuario; }
    public void setTipoPublicacion(String tipoPublicacion) { this.tipoPublicacion = tipoPublicacion; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecioCreditos(int precioCreditos) { this.precioCreditos = precioCreditos; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setEsVecinoDestacado(boolean esVecinoDestacado) { this.esVecinoDestacado = esVecinoDestacado; }
    public void setIdSolicitante(String idSolicitante) { this.idSolicitante = idSolicitante; }
    public void setNombreSolicitante(String nombreSolicitante) { this.nombreSolicitante = nombreSolicitante; }
    public void setEstadoSolicitud(String estadoSolicitud) { this.estadoSolicitud = estadoSolicitud; }
    public void setFechaLimiteRespuesta(long fechaLimiteRespuesta) { this.fechaLimiteRespuesta = fechaLimiteRespuesta; }
}
