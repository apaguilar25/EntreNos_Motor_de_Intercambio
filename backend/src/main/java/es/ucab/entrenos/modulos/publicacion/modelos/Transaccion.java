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
    private long fechaCreacion;
    private String idIncidencia;
    private Integer calificacionOfertante;
    private String comentarioOfertante;
    private boolean sancionado;
    private int version;

    public Transaccion() {}

    public Transaccion(String idPublicacion, String idOfertante, String idDemandante, int creditosRetenidos) {
        this.idTransaccion = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.idPublicacion = idPublicacion;
        this.idOfertante = idOfertante;
        this.idDemandante = idDemandante;
        this.creditosRetenidos = creditosRetenidos;
        this.estado = EstadoTransaccion.PENDIENTE;
        this.confirmacionOfertante = false;
        this.confirmacionDemandante = false;
        this.fechaCreacion = System.currentTimeMillis();
    }

    public void confirmarEntregaOfertante() {
        if (this.estado == EstadoTransaccion.EN_DISPUTA || this.estado == EstadoTransaccion.FINALIZADA) {
            throw new IllegalStateException("No se puede confirmar en estado " + this.estado + ".");
        }
        if (this.idIncidencia != null) {
            throw new IllegalStateException("No se puede confirmar mientras exista una incidencia activa.");
        }
        this.confirmacionOfertante = true;
        verificarCompletitud();
    }

    public void confirmarRecepcionDemandante() {
        if (this.estado == EstadoTransaccion.EN_DISPUTA || this.estado == EstadoTransaccion.FINALIZADA) {
            throw new IllegalStateException("No se puede confirmar en estado " + this.estado + ".");
        }
        if (this.idIncidencia != null) {
            throw new IllegalStateException("No se puede confirmar mientras exista una incidencia activa.");
        }
        this.confirmacionDemandante = true;
        verificarCompletitud();
    }

    private void verificarCompletitud() {
        if (this.confirmacionOfertante && this.confirmacionDemandante) {
            this.estado = EstadoTransaccion.FINALIZADA;
        }
    }

    public void asignarIncidencia(String idIncidencia) {
        if (this.estado != EstadoTransaccion.INICIADA) {
            throw new IllegalStateException("Solo se puede reportar una incidencia en transacciones INICIADAS.");
        }
        this.idIncidencia = idIncidencia;
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
    public long getFechaCreacion() { return fechaCreacion; }
    public String getIdIncidencia() { return idIncidencia; }
    public Integer getCalificacionOfertante() { return calificacionOfertante; }
    public String getComentarioOfertante() { return comentarioOfertante; }
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
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setIdIncidencia(String idIncidencia) { this.idIncidencia = idIncidencia; }
    public void setCalificacionOfertante(Integer calificacionOfertante) { this.calificacionOfertante = calificacionOfertante; }
    public void setComentarioOfertante(String comentarioOfertante) { this.comentarioOfertante = comentarioOfertante; }
    public void setSancionado(boolean sancionado) { this.sancionado = sancionado; }
}
