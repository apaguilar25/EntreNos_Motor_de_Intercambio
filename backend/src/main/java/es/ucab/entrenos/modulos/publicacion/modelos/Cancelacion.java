package es.ucab.entrenos.modulos.publicacion.modelos;

import java.util.UUID;

public class Cancelacion {

    private String idCancelacion;
    private String idTransaccion;
    private String idSolicitante;
    private String idOfertante;
    private String idDemandante;
    private MotivoCancelacion motivo;
    private EstadoCancelacion estado;
    private long fechaCreacion;
    private int version;

    public Cancelacion() {}

    public Cancelacion(String idTransaccion, String idSolicitante, String idOfertante, String idDemandante, MotivoCancelacion motivo) {
        this.idCancelacion = "CAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.idTransaccion = idTransaccion;
        this.idSolicitante = idSolicitante;
        this.idOfertante = idOfertante;
        this.idDemandante = idDemandante;
        this.motivo = motivo;
        this.estado = EstadoCancelacion.PENDIENTE;
        this.fechaCreacion = System.currentTimeMillis();
    }

    public void aceptar() {
        if (this.estado != EstadoCancelacion.PENDIENTE) {
            throw new IllegalStateException("No hay una solicitud de cancelación pendiente.");
        }
        this.estado = EstadoCancelacion.ACEPTADA;
    }

    public void rechazar() {
        if (this.estado != EstadoCancelacion.PENDIENTE) {
            throw new IllegalStateException("No hay una solicitud de cancelación pendiente.");
        }
        this.estado = EstadoCancelacion.RECHAZADA;
    }

    public String getIdCancelacion() { return idCancelacion; }
    public void setIdCancelacion(String idCancelacion) { this.idCancelacion = idCancelacion; }

    public String getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }

    public String getIdSolicitante() { return idSolicitante; }
    public void setIdSolicitante(String idSolicitante) { this.idSolicitante = idSolicitante; }

    public String getIdOfertante() { return idOfertante; }
    public void setIdOfertante(String idOfertante) { this.idOfertante = idOfertante; }

    public String getIdDemandante() { return idDemandante; }
    public void setIdDemandante(String idDemandante) { this.idDemandante = idDemandante; }

    public MotivoCancelacion getMotivo() { return motivo; }
    public void setMotivo(MotivoCancelacion motivo) { this.motivo = motivo; }

    public EstadoCancelacion getEstado() { return estado; }
    public void setEstado(EstadoCancelacion estado) { this.estado = estado; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
