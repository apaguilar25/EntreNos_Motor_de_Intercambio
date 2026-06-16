package es.ucab.entrenos.modulos.publicacion.modelos;

import es.ucab.entrenos.modulos.reputacion.modelos.Resena;
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
    private Resena resena;
    private String idSolicitanteCancelacion;
    private String motivoCancelacion;
    private boolean cancelacionAceptada;
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
        if (this.estado == EstadoTransaccion.PENDIENTE) {
            this.estado = EstadoTransaccion.INICIADA;
        }
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
        if (this.estado == EstadoTransaccion.PENDIENTE) {
            this.estado = EstadoTransaccion.INICIADA;
        }
        verificarCompletitud();
    }

    private void verificarCompletitud() {
        if (this.confirmacionOfertante && this.confirmacionDemandante) {
            this.estado = EstadoTransaccion.FINALIZADA;
        }
    }

    public void solicitarCancelacion(String idSolicitante, String motivoCancelacion) {
        if (this.estado == EstadoTransaccion.FINALIZADA || this.estado == EstadoTransaccion.RECHAZADA || this.estado == EstadoTransaccion.EN_DISPUTA) {
            throw new IllegalStateException("No se puede solicitar cancelación en estado " + this.estado + ".");
        }
        this.idSolicitanteCancelacion = idSolicitante;
        this.motivoCancelacion = motivoCancelacion;
    }

    public void aceptarCancelacion() {
        if (this.idSolicitanteCancelacion == null) {
            throw new IllegalStateException("No hay una solicitud de cancelación pendiente.");
        }
        this.cancelacionAceptada = true;
        this.estado = EstadoTransaccion.RECHAZADA;
    }

    public void rehusarCancelacion() {
        if (this.idSolicitanteCancelacion == null) {
            throw new IllegalStateException("No hay una solicitud de cancelación pendiente.");
        }
        this.idSolicitanteCancelacion = null;
        this.motivoCancelacion = null;
    }

    public void asignarIncidencia(String idIncidencia) {
        if (this.estado != EstadoTransaccion.INICIADA) {
            throw new IllegalStateException("Solo se puede reportar una incidencia en transacciones INICIADAS.");
        }
        this.idIncidencia = idIncidencia;
        this.estado = EstadoTransaccion.EN_DISPUTA;
        this.idSolicitanteCancelacion = null;
        this.motivoCancelacion = null;
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
    public Integer getCalificacion() { return resena != null ? resena.getCalificacion() : null; }
    public Resena getResena() { return resena; }
    public String getIdSolicitanteCancelacion() { return idSolicitanteCancelacion; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public boolean isCancelacionAceptada() { return cancelacionAceptada; }

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
    public void setResena(Resena resena) { this.resena = resena; }
    public void setIdSolicitanteCancelacion(String idSolicitanteCancelacion) { this.idSolicitanteCancelacion = idSolicitanteCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public void setCancelacionAceptada(boolean cancelacionAceptada) { this.cancelacionAceptada = cancelacionAceptada; }
}
