package es.ucab.entrenos.modulos.publicacion.modelos;

import java.util.UUID;

public class Transaccion {

    private String idTransaccion;
    private String idPublicacion;
    private String idOfertante;
    private String idDemandante;
    private EstadoTransaccion estado;
    private int creditosRetenidos;
    private boolean confirmacionOfertante;
    private boolean confirmacionDemandante;
    private String nombreServicio;
    private String descripcion;
    private long fechaCreacion;
    private long fechaLimiteAceptacion;
    private boolean tieneIncidencia;
    private String descripcionIncidencia;
    private String urlEvidenciaIncidencia;
    private Integer calificacionOfertante;
    private String comentarioOfertante;
    private long fechaConfirmacionOfertante;
    private long fechaConfirmacionDemandante;
    private boolean sancionado;
    private int version;

    public Transaccion() {}

    public Transaccion(String idPublicacion, String idOfertante, String idDemandante,
                       String nombreServicio, String descripcion, int creditosRetenidos) {
        this.idTransaccion = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.idPublicacion = idPublicacion;
        this.idOfertante = idOfertante;
        this.idDemandante = idDemandante;
        this.nombreServicio = nombreServicio;
        this.descripcion = descripcion;
        this.creditosRetenidos = creditosRetenidos;
        this.estado = EstadoTransaccion.PENDIENTE;
        this.confirmacionOfertante = false;
        this.confirmacionDemandante = false;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaLimiteAceptacion = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000);
        this.tieneIncidencia = false;
    }

    public void confirmarEntregaOfertante() {
        if (this.estado == EstadoTransaccion.EN_DISPUTA || this.estado == EstadoTransaccion.FINALIZADA) {
            throw new IllegalStateException("No se puede confirmar en estado " + this.estado + ".");
        }
        if (this.tieneIncidencia) {
            throw new IllegalStateException("No se puede confirmar mientras exista una incidencia activa.");
        }
        if (this.estado == EstadoTransaccion.PENDIENTE) {
            this.estado = EstadoTransaccion.INICIADA;
        }
        this.confirmacionOfertante = true;
        verificarCompletitud();
    }

    public void confirmarRecepcionDemandante() {
        if (this.estado == EstadoTransaccion.EN_DISPUTA || this.estado == EstadoTransaccion.FINALIZADA) {
            throw new IllegalStateException("No se puede confirmar en estado " + this.estado + ".");
        }
        if (this.tieneIncidencia) {
            throw new IllegalStateException("No se puede confirmar mientras exista una incidencia activa.");
        }
        if (this.estado == EstadoTransaccion.PENDIENTE) {
            this.estado = EstadoTransaccion.INICIADA;
        }
        this.confirmacionDemandante = true;
        verificarCompletitud();
    }

    private void verificarCompletitud() {
        if (this.confirmacionOfertante && this.confirmacionDemandante) {
            this.estado = EstadoTransaccion.FINALIZADA;
        }
    }

    public boolean haExpiradoPlazoAceptacion() {
        return this.estado == EstadoTransaccion.PENDIENTE
                && System.currentTimeMillis() > this.fechaLimiteAceptacion;
    }

    public void reportarIncidencia(String descripcion, String urlEvidencia) {
        if (this.estado != EstadoTransaccion.INICIADA) {
            throw new IllegalStateException("Solo se puede reportar una incidencia en transacciones INICIADAS.");
        }
        if (descripcion == null || descripcion.trim().length() < 20) {
            throw new IllegalArgumentException("La descripción del incidente debe tener al menos 20 caracteres.");
        }
        this.tieneIncidencia = true;
        this.descripcionIncidencia = descripcion;
        this.urlEvidenciaIncidencia = urlEvidencia;
        this.estado = EstadoTransaccion.EN_DISPUTA;
    }

    public String getIdTransaccion() { return idTransaccion; }
    public String getIdPublicacion() { return idPublicacion; }
    public String getIdOfertante() { return idOfertante; }
    public String getIdDemandante() { return idDemandante; }
    public EstadoTransaccion getEstado() { return estado; }
    public int getCreditosRetenidos() { return creditosRetenidos; }
    public boolean isConfirmacionOfertante() { return confirmacionOfertante; }
    public boolean isConfirmacionDemandante() { return confirmacionDemandante; }
    public String getNombreServicio() { return nombreServicio; }
    public String getDescripcion() { return descripcion; }
    public long getFechaCreacion() { return fechaCreacion; }
    public long getFechaLimiteAceptacion() { return fechaLimiteAceptacion; }
    public boolean isTieneIncidencia() { return tieneIncidencia; }
    public String getDescripcionIncidencia() { return descripcionIncidencia; }
    public String getUrlEvidenciaIncidencia() { return urlEvidenciaIncidencia; }
    public Integer getCalificacionOfertante() { return calificacionOfertante; }
    public String getComentarioOfertante() { return comentarioOfertante; }
    public long getFechaConfirmacionOfertante() { return fechaConfirmacionOfertante; }
    public long getFechaConfirmacionDemandante() { return fechaConfirmacionDemandante; }
    public boolean isSancionado() { return sancionado; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public void setIdPublicacion(String idPublicacion) { this.idPublicacion = idPublicacion; }
    public void setIdOfertante(String idOfertante) { this.idOfertante = idOfertante; }
    public void setIdDemandante(String idDemandante) { this.idDemandante = idDemandante; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }
    public void setCreditosRetenidos(int creditosRetenidos) { this.creditosRetenidos = creditosRetenidos; }
    public void setConfirmacionOfertante(boolean confirmacionOfertante) { this.confirmacionOfertante = confirmacionOfertante; }
    public void setConfirmacionDemandante(boolean confirmacionDemandante) { this.confirmacionDemandante = confirmacionDemandante; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaLimiteAceptacion(long fechaLimiteAceptacion) { this.fechaLimiteAceptacion = fechaLimiteAceptacion; }
    public void setTieneIncidencia(boolean tieneIncidencia) { this.tieneIncidencia = tieneIncidencia; }
    public void setDescripcionIncidencia(String descripcionIncidencia) { this.descripcionIncidencia = descripcionIncidencia; }
    public void setUrlEvidenciaIncidencia(String urlEvidenciaIncidencia) { this.urlEvidenciaIncidencia = urlEvidenciaIncidencia; }
    public void setCalificacionOfertante(Integer calificacionOfertante) { this.calificacionOfertante = calificacionOfertante; }
    public void setComentarioOfertante(String comentarioOfertante) { this.comentarioOfertante = comentarioOfertante; }
    public void setFechaConfirmacionOfertante(long fechaConfirmacionOfertante) { this.fechaConfirmacionOfertante = fechaConfirmacionOfertante; }
    public void setFechaConfirmacionDemandante(long fechaConfirmacionDemandante) { this.fechaConfirmacionDemandante = fechaConfirmacionDemandante; }
    public void setSancionado(boolean sancionado) { this.sancionado = sancionado; }
}
